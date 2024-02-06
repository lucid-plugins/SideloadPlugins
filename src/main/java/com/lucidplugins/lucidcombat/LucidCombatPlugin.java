package com.lucidplugins.lucidcombat;

import com.example.EthanApiPlugin.Collections.ETileItem;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.google.inject.Provides;
import com.lucidplugins.api.item.SlottedItem;
import com.lucidplugins.api.utils.*;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Predicate;


@Extension
@PluginDescriptor(name = "<html><font color=\"#32CD32\">Lucid </font>Combat</html>", description = "Helps with Combat related stuff", enabledByDefault = false)
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
public class LucidCombatPlugin extends Plugin implements KeyListener
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidCombatTileOverlay overlay;

    @Inject
    private LucidCombatPanelOverlay panelOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private KeyManager keyManager;

    @Inject
    private LucidCombatConfig config;

    private int nextSolidFoodTick = 0;
    private int nextPotionTick = 0;
    private int nextKarambwanTick = 0;

    private boolean eatingToMaxHp = false;

    private boolean drinkingToMaxPrayer = false;

    private int timesBrewedDown = 0;

    private Random random = new Random();

    private int nextHpToRestoreAt = 0;

    private int nextPrayerLevelToRestoreAt = 0;

    private int lastTickActive = 0;

    private int nextReactionTick = 0;

    private int lastFinisherAttempt = 0;

    private int nonSpecWeaponId = -1;

    @Getter
    private Actor lastTarget = null;

    @Getter
    private boolean autoCombatRunning = false;

    @Getter
    private String secondaryStatus = "Starting...";

    @Getter
    private WorldPoint startLocation = null;

    @Getter
    private Map<LocalPoint, Integer> expectedLootLocations = new HashMap<>();

    private LocalPoint lastLootedTile = null;

    private int nextLootAttempt = 0;

    private boolean taskEnded = false;

    private List<NPC> npcsKilled = new ArrayList<>();

    private final List<String> prayerRestoreNames = List.of("Prayer potion", "Super restore", "Sanfew serum", "Blighted super restore");

    private final Predicate<SlottedItem> foodFilterNoBlacklistItems = (item) -> {
        final ItemComposition itemComposition = client.getItemDefinition(item.getItem().getId());
        return itemComposition.getName() != null &&
                (!itemComposition.getName().equals("Cooked karambwan") && !itemComposition.getName().equals("Blighted karambwan")) &&
                !config.foodBlacklist().contains(itemComposition.getName()) &&
                (Arrays.asList(itemComposition.getInventoryActions()).contains("Eat"));
    };

    private final Predicate<SlottedItem> karambwanFilter = (item) -> {
        final ItemComposition itemComposition = client.getItemDefinition(item.getItem().getId());
        return itemComposition.getName() != null &&
                (itemComposition.getName().equals("Cooked karambwan") || itemComposition.getName().equals("Blighted karambwan")) &&
                (Arrays.asList(itemComposition.getInventoryActions()).contains("Eat"));
    };

    @Override
    protected void startUp()
    {
        clientThread.invoke(this::pluginEnabled);
    }

    private void pluginEnabled()
    {
        keyManager.registerKeyListener(this);

        if (!overlayManager.anyMatch(p -> p == overlay))
        {
            overlayManager.add(overlay);
        }

        if (!overlayManager.anyMatch(p -> p == panelOverlay))
        {
            overlayManager.add(panelOverlay);
        }

        expectedLootLocations.clear();
        npcsKilled.clear();
    }

    @Override
    protected void shutDown()
    {
        keyManager.unregisterKeyListener(this);
        autoCombatRunning = false;

        if (overlayManager.anyMatch(p -> p == overlay))
        {
            overlayManager.remove(overlay);
        }

        if (overlayManager.anyMatch(p -> p == panelOverlay))
        {
            overlayManager.remove(panelOverlay);
        }
    }

    @Provides
    LucidCombatConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucidCombatConfig.class);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("lucid-combat"))
        {
            return;
        }

        clientThread.invoke(() -> {
            lastTickActive = client.getTickCount();
            taskEnded = false;
            nextHpToRestoreAt = Math.max(1, config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0));
            nextPrayerLevelToRestoreAt = Math.max(1, config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0));
        });
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (config.specIfEquipped() && event.getMenuOption().equals("Wield") && (!config.specWeapon().isEmpty() && event.getMenuTarget().contains(config.specWeapon())))
        {
            lastTarget = client.getLocalPlayer().getInteracting();

            if (EquipmentUtils.getWepSlotItem() != null)
            {
                nonSpecWeaponId = EquipmentUtils.getWepSlotItem().getId();
            }
        }

    }

    @Subscribe
    private void onChatMessage(ChatMessage event)
    {
        if (event.getType() == ChatMessageType.GAMEMESSAGE && (event.getMessage().contains("return to a Slayer master") || event.getMessage().contains("more advanced Slayer Master")))
        {

            if (config.stopOnTaskCompletion() && autoCombatRunning)
            {
                secondaryStatus = "Slayer Task Done";
                startLocation = null;
                autoCombatRunning = false;
                taskEnded = true;
                lastTarget = null;
            }

            if (config.stopUpkeepOnTaskCompletion() && taskEnded)
            {
                expectedLootLocations.clear();
                lastTickActive = 0;
            }

            if (config.teletabOnCompletion() && taskEnded)
            {
                Optional<SlottedItem> teletab = InventoryUtils.getAll(item -> {
                    ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                    return Arrays.asList(composition.getInventoryActions()).contains("Break") && composition.getName().toLowerCase().contains("teleport");
                }).stream().findFirst();

                teletab.ifPresent(tab -> InventoryUtils.itemInteract(tab.getItem().getId(), "Break"));
            }
        }

        if (event.getType() == ChatMessageType.GAMEMESSAGE && (event.getMessage().contains("can't take items that other") || event.getMessage().contains("have enough inventory space")))
        {
            expectedLootLocations.keySet().removeIf(tile -> tile.equals(lastLootedTile));
        }
    }

    @Subscribe
    private void onClientTick(ClientTick tick)
    {
        if (client.getLocalPlayer().getInteracting() != null && client.getLocalPlayer().getInteracting().getHealthRatio() == 0)
        {
            if (client.getLocalPlayer().getInteracting() instanceof NPC)
            {
                if (!npcsKilled.contains((NPC)client.getLocalPlayer().getInteracting()))
                {
                    npcsKilled.add((NPC)client.getLocalPlayer().getInteracting());
                }
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick tick)
    {
        expectedLootLocations.entrySet().removeIf(i -> client.getTickCount() > i.getValue() + 100);

        if (client.getGameState() != GameState.LOGGED_IN || BankUtils.isOpen())
        {
            return;
        }

        updatePluginVars();

        if (hpFull() && eatingToMaxHp)
        {
            secondaryStatus = "HP Full Now";
            eatingToMaxHp = false;
        }

        if (prayerFull() && drinkingToMaxPrayer)
        {
            secondaryStatus = "Prayer Full Now";
            drinkingToMaxPrayer = false;
        }

        boolean actionTakenThisTick = restorePrimaries();

        // Stop other upkeep besides HP if we haven't animated in the last minute
        if (getInactiveTicks() > 200)
        {
            secondaryStatus = "Idle for > 2 min";
            return;
        }


        if (!actionTakenThisTick)
        {
            actionTakenThisTick = restoreStats();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Restoring Stats";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = restoreBoosts();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Restoring Boosts";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleSlayerFinisher();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Finishing Slayer Monster";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleAutoSpec();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Auto-Spec";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleLooting();

            if (!actionTakenThisTick && (nextLootAttempt - client.getTickCount()) < 0 && lastTarget != null)
            {
                actionTakenThisTick = handleReAttack();
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleAutoCombat();
        }

    }

    public boolean isMoving()
    {
        return client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation();
    }

    private boolean handleReAttack()
    {
        if (lastTarget != null && !isMoving())
        {
            if (lastTarget instanceof Player && (lastTarget.getInteracting() == client.getLocalPlayer() && client.getLocalPlayer().getInteracting() != lastTarget))
            {
                if (isPlayerEligible((Player)lastTarget))
                {
                    PlayerUtils.interactPlayer(lastTarget.getName(), "Attack");
                    lastTarget = null;
                    secondaryStatus = "Re-attacking previous target";
                    return true;
                }
            }
            else if (lastTarget instanceof NPC && (lastTarget.getInteracting() == client.getLocalPlayer() && client.getLocalPlayer().getInteracting() != lastTarget))
            {
                if (isNpcEligible((NPC)lastTarget))
                {
                    NpcUtils.interact((NPC)lastTarget, "Attack");
                    lastTarget = null;
                    secondaryStatus = "Re-attacking previous target";
                    return true;
                }
            }
        }

        return false;
    }

    private boolean handleSlayerFinisher()
    {
        Actor target = client.getLocalPlayer().getInteracting();
        if (target instanceof NPC)
        {
            NPC npcTarget = (NPC) target;
            int ratio = npcTarget.getHealthRatio();
            int scale = npcTarget.getHealthScale();

            double targetHpPercent = Math.floor((double) ratio  / (double) scale * 100);
            if (targetHpPercent < config.slayerFinisherHpPercent() && targetHpPercent >= 0)
            {
                Item slayerFinisher = InventoryUtils.getFirstItem(config.slayerFinisherItem().getItemName());
                if (config.autoSlayerFinisher() && slayerFinisher != null &&
                        client.getTickCount() - lastFinisherAttempt > 5)
                {
                    InteractionUtils.useItemOnNPC(slayerFinisher.getId(), npcTarget);
                    lastFinisherAttempt = client.getTickCount();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleAutoSpec()
    {
        if (!config.enableAutoSpec() || config.specWeapon().isEmpty())
        {
            return false;
        }

        if (nonSpecWeaponId != -1 && !canSpec())
        {
            lastTarget = client.getLocalPlayer().getInteracting();
            InventoryUtils.itemInteract(nonSpecWeaponId, "Wield");
            nonSpecWeaponId = -1;
            return true;
        }

        boolean equippedItem = false;
        if (!EquipmentUtils.contains(config.specWeapon()))
        {
            if (!config.specIfEquipped())
            {
                Item specWeapon = InventoryUtils.getFirstItem(config.specWeapon());
                if (specWeapon != null && canSpec())
                {
                    if (EquipmentUtils.getWepSlotItem() != null)
                    {
                        nonSpecWeaponId = EquipmentUtils.getWepSlotItem().getId();
                    }

                    lastTarget = client.getLocalPlayer().getInteracting();

                    if (lastTarget != null)
                    {
                        InventoryUtils.itemInteract(specWeapon.getId(), "Wield");
                        equippedItem = true;
                    }
                }
            }
        }
        else
        {
            if (client.getLocalPlayer().getInteracting() != null || lastTarget != null)
            {
                equippedItem = true;
            }
        }

        if (equippedItem && canSpec() && !CombatUtils.isSpecEnabled())
        {
            CombatUtils.toggleSpec();
            return true;
        }

        return false;
    }

    private boolean canSpec()
    {
        final int spec = CombatUtils.getSpecEnergy(client);
        return spec >= config.minSpec() && spec >= config.specNeeded();
    }

    private boolean handleLooting()
    {
        if (!autoCombatRunning)
        {
            return false;
        }

        if (nextLootAttempt == 0)
        {
            nextLootAttempt = client.getTickCount();
        }

        if (ticksUntilNextLootAttempt() > 0)
        {
            return false;
        }

        boolean ignoringTargetLimitation = ticksUntilNextLootAttempt() < -config.maxTicksBetweenLooting();

        if (config.onlyLootWithNoTarget() && !(targetDeadOrNoTargetIgnoreAttackingUs() || ignoringTargetLimitation))
        {
            return false;
        }

        List<ETileItem> lootableItems = InteractionUtils.getAllTileItems(tileItem -> {
            ItemComposition composition = client.getItemDefinition(tileItem.getTileItem().getId());
            boolean nameContains = false;
            for (String itemName : config.lootNames().split(","))
            {
                itemName = itemName.trim();

                if (composition.getName() != null && composition.getName().contains(itemName))
                {
                    nameContains = true;
                    break;
                }
            }

            boolean inBlacklist = false;
            if (!config.lootBlacklist().trim().isEmpty())
            {
                for (String itemName : config.lootBlacklist().split(","))
                {
                    itemName = itemName.trim();

                    if (itemName.length() < 2)
                    {
                        continue;
                    }

                    if (composition.getName() != null && composition.getName().contains(itemName))
                    {
                        inBlacklist = true;
                        break;
                    }
                }
            }

            boolean antiLureActivated = false;

            if (config.antilureProtection())
            {
                antiLureActivated = InteractionUtils.distanceTo2DHypotenuse(tileItem.getLocation(), startLocation) > (config.maxRange() + 3);
            }

            return (!inBlacklist && nameContains) && expectedLootLocations.containsKey(LocalPoint.fromWorld(client, tileItem.getLocation())) &&
                    InteractionUtils.distanceTo2DHypotenuse(tileItem.getLocation(), client.getLocalPlayer().getWorldLocation()) <= config.lootRange() &&
                    !antiLureActivated;
        });

        if (config.stackableOnly())
        {
            lootableItems.removeIf(loot -> {

                if (config.buryScatter())
                {
                    return (!isStackable(loot.getTileItem().getId()) && !canBuryOrScatter(loot.getTileItem().getId())) || (loot.getTileItem().getId() == ItemID.CURVED_BONE || loot.getTileItem().getId() == ItemID.LONG_BONE);
                }

                return !isStackable(loot.getTileItem().getId());
            });
        }

        if (InventoryUtils.getFreeSlots() == 0)
        {
            lootableItems.removeIf(loot -> !isStackable(loot.getTileItem().getId()) || (isStackable(loot.getTileItem().getId()) && InventoryUtils.count(loot.getTileItem().getId()) == 0));
        }

        ETileItem nearest = nearestTileItem(lootableItems);

        if (config.enableLooting() && nearest != null)
        {
            if (client.getLocalPlayer().getInteracting() != null)
            {
                lastTarget = client.getLocalPlayer().getInteracting();
            }

            InteractionUtils.interactWithTileItem(nearest.getTileItem().getId(), "Take");
            lastLootedTile = LocalPoint.fromWorld(client, nearest.getLocation());

            if (!client.getLocalPlayer().getLocalLocation().equals(LocalPoint.fromWorld(client, nearest.getLocation())))
            {
                if (config.onlyLootWithNoTarget())
                {
                    if (ignoringTargetLimitation && lootableItems.size() <= 1)
                    {
                        nextLootAttempt = client.getTickCount() + 2;
                    }
                }
                else
                {
                    nextLootAttempt = client.getTickCount() + 2;
                }
            }
            else
            {
                if (config.onlyLootWithNoTarget())
                {
                    if (ignoringTargetLimitation && lootableItems.size() <= 1)
                    {
                        nextLootAttempt = client.getTickCount() + 2;
                    }
                }
            }

            secondaryStatus = "Looting!";
            return true;
        }


        if (config.buryScatter())
        {
            List<SlottedItem> itemsToBury = InventoryUtils.getAll(item -> {
                ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                return Arrays.asList(composition.getInventoryActions()).contains("Bury") &&
                        !(composition.getName().contains("Long") || composition.getName().contains("Curved"));
            });

            List<SlottedItem> itemsToScatter = InventoryUtils.getAll(item -> {
                ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                return Arrays.asList(composition.getInventoryActions()).contains("Scatter");
            });

            if (!itemsToBury.isEmpty())
            {
                SlottedItem itemToBury = itemsToBury.get(0);

                if (itemToBury != null)
                {
                    InventoryUtils.itemInteract(itemToBury.getItem().getId(), "Bury");
                    nextReactionTick = client.getTickCount() + randomIntInclusive(1, 3);
                    return true;
                }
            }

            if (!itemsToScatter.isEmpty())
            {
                SlottedItem itemToScatter = itemsToScatter.get(0);

                if (itemToScatter != null)
                {
                    InventoryUtils.itemInteract(itemToScatter.getItem().getId(), "Scatter");
                    nextReactionTick = client.getTickCount() + randomIntInclusive(1, 3);
                    return true;
                }
            }
        }

        return false;
    }

    private ETileItem nearestTileItem(List<ETileItem> items)
    {
        ETileItem nearest = null;
        float nearestDist = 999;

        for (ETileItem tileItem : items)
        {
            final float dist = InteractionUtils.distanceTo2DHypotenuse(tileItem.getLocation(), client.getLocalPlayer().getWorldLocation());
            if (dist < nearestDist)
            {
                nearest = tileItem;
                nearestDist = dist;
            }
        }

        return nearest;
    }

    private boolean lootableItems()
    {
        return false;
    }

    private boolean handleAutoCombat()
    {
        if (!autoCombatRunning)
        {
            return false;
        }

        if (!canReact() || isMoving())
        {
            return false;
        }

        if (ticksUntilNextLootAttempt() > 0)
        {
            return false;
        }

        secondaryStatus = "Combat";

        if (targetDeadOrNoTarget())
        {
            NPC target = getEligibleTarget();
            if (target != null)
            {
                NpcUtils.interact(target, "Attack");
                nextReactionTick = client.getTickCount() + getReaction();
                secondaryStatus = "Attacking " + target.getName();

                if (getInactiveTicks() > 2)
                {
                    lastTickActive = client.getTickCount();
                }

                return true;
            }
            else
            {
                secondaryStatus = "Nothing to murder";
                nextReactionTick = client.getTickCount() + getReaction();
                return false;
            }
        }
        else
        {
            if (getEligibleNpcInteractingWithUs() != null && client.getLocalPlayer().getInteracting() == null)
            {
                if (isNpcEligible(getEligibleNpcInteractingWithUs()))
                {
                    NpcUtils.interact(getEligibleNpcInteractingWithUs(), "Attack");
                    nextReactionTick = client.getTickCount() + getReaction();
                    secondaryStatus = "Re-attacking " + getEligibleNpcInteractingWithUs().getName();
                }

                if (getInactiveTicks() > 2)
                {
                    lastTickActive = client.getTickCount();
                }
                return true;
            }
        }

        secondaryStatus = "Idle";
        nextReactionTick = client.getTickCount() + getReaction();
        return false;
    }

    public int getReaction()
    {
        int min = config.autocombatStyle().getLowestDelay();
        int max = config.autocombatStyle().getHighestDelay();

        int delay = randomIntInclusive(min, max);

        if (config.autocombatStyle() == PlayStyle.ROBOTIC)
        {
            delay = 0;
        }

        int randomMinDelay = Math.max(0, randomStyle().getLowestDelay());
        int randomMaxDelay = Math.max(randomMinDelay, randomStyle().getHighestDelay());

        int randomDeterminer = randomIntInclusive(0, 49);

        if (config.reactionAntiPattern())
        {
            boolean fiftyFifty = randomIntInclusive(0, 1) == 0;
            int firstNumber = (fiftyFifty ? 5 : 18);
            int secondNumber = (fiftyFifty ? 24 : 48);
            if (randomDeterminer == firstNumber || randomDeterminer == secondNumber)
            {
                delay = randomIntInclusive(randomMinDelay, randomMaxDelay);
                random = new Random();
            }
        }

        return delay;
    }

    public PlayStyle randomStyle()
    {
        return PlayStyle.values()[randomIntInclusive(0, PlayStyle.values().length - 1)];
    }

    public int randomIntInclusive(int min, int max)
    {
        return random.nextInt((max - min) + 1) + min;
    }


    private boolean canReact()
    {
        return ticksUntilNextInteraction() <= 0;
    }

    public int ticksUntilNextInteraction()
    {
        return nextReactionTick - client.getTickCount();
    }


    private NPC getEligibleTarget()
    {
        if (config.npcToFight().isEmpty())
        {
            return null;
        }

        return NpcUtils.getNearestNpc(npc -> {
            boolean nameContains = false;
            for (String npcName : config.npcToFight().split(","))
            {
                npcName = npcName.trim();

                if (npc.getName() != null && npc.getName().contains(npcName))
                {
                    nameContains = true;
                    break;
                }
            }

            return nameContains &&

                    (((npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)) ||
                            (npc.getInteracting() == null && noPlayerFightingNpc(npc)) ||
                            (npc.getInteracting() instanceof NPC && noPlayerFightingNpc(npc))) &&

                    Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
                    InteractionUtils.isWalkable(npc.getWorldLocation()) &&
                    InteractionUtils.distanceTo2DHypotenuse(npc.getWorldLocation(), startLocation) <= config.maxRange();
        });
    }

    private boolean isNpcEligible(NPC npc)
    {
        if (npc.getComposition().getActions() == null)
        {
            return false;
        }

        boolean nameContains = false;
        for (String npcName : config.npcToFight().split(","))
        {
            npcName = npcName.trim();

            if (npc.getName() != null && npc.getName().contains(npcName))
            {
                nameContains = true;
                break;
            }
        }

        return nameContains &&

                (((npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)) ||
                        (npc.getInteracting() == null && noPlayerFightingNpc(npc)) ||
                        (npc.getInteracting() instanceof NPC && noPlayerFightingNpc(npc))) &&

                Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
                InteractionUtils.isWalkable(npc.getWorldLocation()) &&
                InteractionUtils.distanceTo2DHypotenuse(npc.getWorldLocation(), startLocation) <= config.maxRange();
    }

    private boolean isPlayerEligible(Player player)
    {
        return Arrays.asList(client.getPlayerOptions()).contains("Attack");
    }

    private boolean noPlayerFightingNpc(NPC npc)
    {
        return PlayerUtils.getNearest(player -> player != client.getLocalPlayer() && player.getInteracting() == npc || npc.getInteracting() == player) == null;
    }

    private boolean targetDeadOrNoTarget()
    {
        NPC interactingWithUs = getEligibleNpcInteractingWithUs();

        if (client.getLocalPlayer().getInteracting() == null && interactingWithUs == null)
        {
            return true;
        }

        if (interactingWithUs != null)
        {
            return false;
        }

        if (client.getLocalPlayer().getInteracting() instanceof NPC)
        {
            NPC npcTarget = (NPC) client.getLocalPlayer().getInteracting();
            int ratio = npcTarget.getHealthRatio();

            return ratio == 0;
        }

        return false;
    }


    private boolean targetDeadOrNoTargetIgnoreAttackingUs()
    {
        if (client.getLocalPlayer().getInteracting() == null)
        {
            return true;
        }

        if (client.getLocalPlayer().getInteracting() instanceof NPC)
        {
            NPC npcTarget = (NPC) client.getLocalPlayer().getInteracting();
            int ratio = npcTarget.getHealthRatio();

            return ratio == 0;
        }

        return false;
    }

    private NPC getEligibleNpcInteractingWithUs()
    {
        return NpcUtils.getNearestNpc((npc) ->
        {
            boolean nameContains = false;
            for (String npcName : config.npcToFight().split(","))
            {
                npcName = npcName.trim();

                if (npc.getName() != null && npc.getName().contains(npcName))
                {
                    nameContains = true;
                    break;
                }
            }

            return nameContains &&
                    (npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0) &&
                    Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
                    InteractionUtils.isWalkable(npc.getWorldLocation()) &&
                    InteractionUtils.distanceTo2DHypotenuse(npc.getWorldLocation(), startLocation) <= config.maxRange();
        });
    }

    @Subscribe
    private void onNpcLootReceived(NpcLootReceived event)
    {
        boolean match = false;
        for (NPC killed : npcsKilled)
        {
            if (event.getNpc() == killed)
            {
                match = true;
                break;
            }
        }

        if (!match)
        {
            return;
        }

        npcsKilled.remove(event.getNpc());

        if (event.getItems().size() > 0)
        {
            List<ItemStack> itemStacks = new ArrayList<>(event.getItems());
            for (ItemStack itemStack : itemStacks)
            {
                if (expectedLootLocations.getOrDefault(itemStack.getLocation(), null) == null)
                {
                    expectedLootLocations.put(itemStack.getLocation(), client.getTickCount());
                }
            }
        }
    }

    private void updatePluginVars()
    {
        if (client.getLocalPlayer().getAnimation() != -1)
        {
            if ((config.stopUpkeepOnTaskCompletion() && !taskEnded) || !config.stopUpkeepOnTaskCompletion())
            {
                lastTickActive = client.getTickCount();
            }
        }

        if (nextHpToRestoreAt <= 0)
        {
            nextHpToRestoreAt = Math.max(1, config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0));
        }

        if (nextPrayerLevelToRestoreAt <= 0)
        {
            nextPrayerLevelToRestoreAt = Math.max(1, config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0));
        }
    }

    public WorldPoint getCenter(WorldArea area)
    {
        if (area.getWidth() < 3)
        {
            return new WorldPoint(area.getX(), area.getY(), area.getPlane());
        }

        return new WorldPoint(area.getX() + area.getWidth() / 2, area.getY() + area.getHeight() / 2, area.getPlane());
    }

    private boolean restorePrimaries()
    {
        boolean ateFood = false;
        boolean restoredPrayer = false;
        boolean brewed = false;
        boolean karambwanned = false;

        if (config.enableHpRestore() && needToRestoreHp())
        {
            final List<SlottedItem> foodItems = getFoodItemsNotInBlacklist();
            if (!foodItems.isEmpty() && canRestoreHp())
            {
                if (!eatingToMaxHp && config.restoreHpToMax())
                {
                    eatingToMaxHp = true;
                }

                final SlottedItem firstItem = foodItems.get(0);
                InventoryUtils.itemInteract(firstItem.getItem().getId(), "Eat");

                ateFood = true;
            }

            if ((!ateFood || config.enableTripleEat()) && canPotUp())
            {
                if (!eatingToMaxHp && config.restoreHpToMax())
                {
                    eatingToMaxHp = true;
                }

                final Item saraBrew = getLowestDosePotion("Saradomin brew");
                if (saraBrew != null)
                {
                    InventoryUtils.itemInteract(saraBrew.getId(), "Drink");
                    brewed = true;
                }
            }
        }

        if (config.enablePrayerRestore() && !brewed && needToRestorePrayer() && canPotUp())
        {
            if (!drinkingToMaxPrayer && config.restorePrayerToMax())
            {
                drinkingToMaxPrayer = true;
            }

            final Item prayerRestore = getLowestDosePrayerRestore();
            if (prayerRestore != null)
            {
                InventoryUtils.itemInteract(prayerRestore.getId(), "Drink");
                restoredPrayer = true;
            }
        }

        if (!restoredPrayer && needToRestoreHp() && canKarambwan())
        {
            boolean shouldEat = false;
            if ((config.enableDoubleEat() || config.enableTripleEat()) && ateFood)
            {
                shouldEat = true;
            }

            if (config.enableHpRestore() && !ateFood && getFoodItemsNotInBlacklist().isEmpty())
            {
                shouldEat = true;
            }

            final SlottedItem karambwan = InventoryUtils.getAll(karambwanFilter).stream().findFirst().orElse(null);

            if (karambwan != null && shouldEat)
            {
                if (!ateFood && !eatingToMaxHp && config.restoreHpToMax())
                {
                    eatingToMaxHp = true;
                }

                InventoryUtils.itemInteract(karambwan.getItem().getId(), "Eat");
                karambwanned = true;
            }
        }

        if (config.stopIfNoFood() && config.enableHpRestore() && needToRestoreHp() && !ateFood && !brewed && !karambwanned)
        {
            if (autoCombatRunning)
            {
                secondaryStatus = "Ran out of food";
                autoCombatRunning = false;
            }
        }

        if (ateFood)
        {
            nextSolidFoodTick = client.getTickCount() + 3;
            nextHpToRestoreAt = config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0);
        }

        if (restoredPrayer)
        {
            nextPotionTick = client.getTickCount() + 3;
            nextPrayerLevelToRestoreAt = config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0);
        }

        if (brewed)
        {
            nextPotionTick = client.getTickCount() + 3;
            timesBrewedDown++;
            nextHpToRestoreAt = config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0);
        }

        if (karambwanned)
        {
            nextKarambwanTick = client.getTickCount() + 2;
        }

        return ateFood || restoredPrayer || brewed || karambwanned;
    }

    private boolean needToRestoreHp()
    {
        final int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        return currentHp < nextHpToRestoreAt || eatingToMaxHp;
    }

    private boolean hpFull()
    {
        final int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
        final int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        return currentHp >= (maxHp - config.maxHpBuffer());
    }


    private boolean needToRestorePrayer()
    {
        final int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        return currentPrayer < nextPrayerLevelToRestoreAt || drinkingToMaxPrayer;
    }

    private boolean prayerFull()
    {
        final int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        final int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        return currentPrayer >= (maxPrayer - config.maxPrayerBuffer());
    }

    private boolean restoreStats()
    {
        if (timesBrewedDown > 2 && canPotUp())
        {
            Item restore = getLowestDoseRestore();
            if (restore != null)
            {
                InventoryUtils.itemInteract(restore.getId(), "Drink");

                nextPotionTick = client.getTickCount() + 3;

                timesBrewedDown -= 3;

                if (timesBrewedDown < 0)
                {
                    timesBrewedDown = 0;
                }

                return true;
            }
        }

        return false;
    }

    private boolean restoreBoosts()
    {
        boolean meleeBoosted = false;
        boolean rangedBoosted = false;
        boolean magicBoosted = false;

        final int attackBoost = client.getBoostedSkillLevel(Skill.ATTACK) - client.getRealSkillLevel(Skill.ATTACK);
        final int strengthBoost = client.getBoostedSkillLevel(Skill.STRENGTH) - client.getRealSkillLevel(Skill.STRENGTH);
        final int defenseBoost = client.getBoostedSkillLevel(Skill.DEFENCE) - client.getRealSkillLevel(Skill.DEFENCE);

        Item meleePotionToUse = null;

        final Item combatBoostPotion = getCombatBoostingPotion();

        if (attackBoost < config.minMeleeBoost())
        {
            final Item attackBoostingItem = getAttackBoostingItem();

            if (attackBoostingItem != null)
            {
                meleePotionToUse = attackBoostingItem;
            }
            else if (combatBoostPotion != null)
            {
                meleePotionToUse = combatBoostPotion;
            }
        }
        else if (strengthBoost < config.minMeleeBoost())
        {
            final Item strengthBoostingItem = getStrengthBoostingItem();
            if (strengthBoostingItem != null)
            {
                meleePotionToUse = strengthBoostingItem;
            }
            else if (combatBoostPotion != null)
            {
                meleePotionToUse = combatBoostPotion;
            }
        }
        else if (defenseBoost < config.minMeleeBoost())
        {
            final Item defenseBoostingItem = getDefenseBoostingItem();
            if (defenseBoostingItem != null)
            {
                meleePotionToUse = defenseBoostingItem;
            }
            else if (combatBoostPotion != null)
            {
                meleePotionToUse = combatBoostPotion;
            }
        }

        if (config.enableMeleeUpkeep() && meleePotionToUse != null && canPotUp())
        {
            InventoryUtils.itemInteract(meleePotionToUse.getId(), "Drink");
            nextPotionTick = client.getTickCount() + 3;
            meleeBoosted = true;
        }

        final int rangedBoost = client.getBoostedSkillLevel(Skill.RANGED) - client.getRealSkillLevel(Skill.RANGED);
        if (rangedBoost < config.minRangedBoost() && !meleeBoosted)
        {
            Item rangedPotion = getRangedBoostingItem();
            if (config.enableRangedUpkeep() && rangedPotion != null && canPotUp())
            {
                InventoryUtils.itemInteract(rangedPotion.getId(), "Drink");
                nextPotionTick = client.getTickCount() + 3;
                rangedBoosted = true;
            }
        }

        final int magicBoost = client.getBoostedSkillLevel(Skill.MAGIC) - client.getRealSkillLevel(Skill.MAGIC);
        if (magicBoost < config.minMagicBoost() && !meleeBoosted && !rangedBoosted)
        {
            Item magicPotion = getMagicBoostingPotion();
            Item imbuedHeart = InventoryUtils.getFirstItem("Imbued heart");
            Item saturatedHeart = InventoryUtils.getFirstItem("Saturated heart");
            Item heart = imbuedHeart != null ? imbuedHeart : saturatedHeart;

            if (config.enableMagicUpkeep() && magicPotion != null && canPotUp())
            {
                InventoryUtils.itemInteract(magicPotion.getId(), "Drink");
                nextPotionTick = client.getTickCount() + 3;
                magicBoosted = true;
            }
            else if (config.enableMagicUpkeep() && imbuedHeartTicksLeft() == 0 && heart != null)
            {
                InventoryUtils.itemInteract(heart.getId(), "Invigorate");
                magicBoosted = true;
            }
        }

        return meleeBoosted || rangedBoosted || magicBoosted;
    }

    private boolean canRestoreHp()
    {
        return client.getTickCount() > nextSolidFoodTick;
    }

    private boolean canPotUp()
    {
        return client.getTickCount() > nextPotionTick;
    }

    private boolean canKarambwan()
    {
        return client.getTickCount() > nextKarambwanTick;
    }

    private List<SlottedItem> getFoodItemsNotInBlacklist()
    {
        return InventoryUtils.getAll(foodFilterNoBlacklistItems);
    }

    private Item getAttackBoostingItem()
    {
        Item itemToUse = null;

        final Item attackPot = getLowestDosePotion("Attack potion");
        final Item superAttackPot = getLowestDosePotion("Super attack");
        final Item divineSuperAttack = getLowestDosePotion("Divine super attack potion");

        if (attackPot != null)
        {
            itemToUse = attackPot;
        }
        else if (superAttackPot != null)
        {
            itemToUse = superAttackPot;
        }
        else if (divineSuperAttack != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineSuperAttack;
        }

        return itemToUse;
    }

    private Item getStrengthBoostingItem()
    {
        Item itemToUse = null;

        final Item strengthPot = getLowestDosePotion("Strength potion");
        final Item superStrengthPot = getLowestDosePotion("Super strength");
        final Item divineSuperStrength = getLowestDosePotion("Divine super strength potion");

        if (strengthPot != null)
        {
            itemToUse = strengthPot;
        }
        else if (superStrengthPot != null)
        {
            itemToUse = superStrengthPot;
        }
        else if (divineSuperStrength != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineSuperStrength;
        }

        return itemToUse;
    }

    private Item getDefenseBoostingItem()
    {
        Item itemToUse = null;

        final Item defensePot = getLowestDosePotion("Defense potion");
        final Item superDefensePot = getLowestDosePotion("Super defense");
        final Item divineSuperDefense = getLowestDosePotion("Divine super defense potion");

        if (defensePot != null)
        {
            itemToUse = defensePot;
        }
        else if (superDefensePot != null)
        {
            itemToUse = superDefensePot;
        }
        else if (divineSuperDefense != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineSuperDefense;
        }

        return itemToUse;
    }

    private Item getRangedBoostingItem()
    {
        Item itemToUse = null;

        final Item rangingPot = getLowestDosePotion("Ranging potion");
        final Item divineRangingPot = getLowestDosePotion("Divine ranging potion");
        final Item bastionPot = getLowestDosePotion("Bastion potion");
        final Item divineBastionPot = getLowestDosePotion("Divine bastion potion");

        if (rangingPot != null)
        {
            itemToUse = rangingPot;
        }
        else if (divineRangingPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineRangingPot;
        }
        else if (bastionPot != null)
        {
            itemToUse = bastionPot;
        }
        else if (divineBastionPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineBastionPot;
        }

        return itemToUse;
    }

    private Item getMagicBoostingPotion()
    {
        Item itemToUse = null;

        final Item magicEssence = getLowestDosePotion("Magic essence");
        final Item magicPot = getLowestDosePotion("Magic potion");
        final Item divineMagicPot = getLowestDosePotion("Divine magic potion");
        final Item battleMagePot = getLowestDosePotion("Battlemage potion");
        final Item divineBattleMagePot = getLowestDosePotion("Divine battlemage potion");

        if (magicEssence != null)
        {
            itemToUse = magicEssence;
        }
        else if (magicPot != null)
        {
            itemToUse = magicPot;
        }
        else if (divineMagicPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineMagicPot;
        }
        else if (battleMagePot != null)
        {
            itemToUse = battleMagePot;
        }
        else if (divineBattleMagePot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineBattleMagePot;
        }

        return itemToUse;
    }

    private int imbuedHeartTicksLeft()
    {
        return client.getVarbitValue(Varbits.IMBUED_HEART_COOLDOWN) * 10;
    }

    private Item getCombatBoostingPotion()
    {
        Item itemToUse = null;

        final Item combatPot = getLowestDosePotion("Combat potion");
        final Item superCombatPot = getLowestDosePotion("Super combat potion");
        final Item divineCombatPot = getLowestDosePotion("Divine super combat potion");

        if (combatPot != null)
        {
            itemToUse = combatPot;
        }
        else if (superCombatPot != null)
        {
            itemToUse = superCombatPot;
        }
        else if (divineCombatPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineCombatPot;
        }

        return itemToUse;
    }

    private Item getLowestDosePotion(String name)
    {
        for (int i = 1; i < 5; i++)
        {
            final String fullName = name + "(" + i + ")";

            if (config.foodBlacklist().contains(fullName))
            {
                continue;
            }

            final Item b = InventoryUtils.getFirstItem(fullName);
            if (b != null)
            {
                final ItemComposition itemComposition = client.getItemDefinition(b.getId());
                if ((Arrays.asList(itemComposition.getInventoryActions()).contains("Drink")))
                {
                    return b;
                }
            }
        }
        return null;
    }

    private Item getLowestDoseRestore()
    {
        for (int i = 1; i < 5; i++)
        {
            final String fullName = "Super restore(" + i + ")";

            if (config.foodBlacklist().contains(fullName))
            {
                continue;
            }

            final Item b = InventoryUtils.getFirstItem(fullName);
            if (b != null)
            {
                final ItemComposition itemComposition = client.getItemDefinition(b.getId());
                if ((Arrays.asList(itemComposition.getInventoryActions()).contains("Drink")))
                {
                    return b;
                }
            }
        }
        return null;
    }

    private Item getLowestDosePrayerRestore()
    {
        for (int i = 1; i < 5; i++)
        {
            for (String restoreItem : prayerRestoreNames)
            {
                String fullName = restoreItem + "(" + i + ")";

                if (config.foodBlacklist().contains(fullName))
                {
                    continue;
                }

                Item r = InventoryUtils.getFirstItem(fullName);
                if (r != null)
                {
                    ItemComposition itemComposition = client.getItemDefinition(r.getId());
                    if ((Arrays.asList(itemComposition.getInventoryActions()).contains("Drink")))
                    {
                        return r;
                    }
                }
            }
        }
        return null;
    }

    public int getInactiveTicks()
    {
        return client.getTickCount() - lastTickActive;
    }

    public int ticksUntilNextLootAttempt()
    {
        return nextLootAttempt - client.getTickCount();
    }

    public float getDistanceToStart()
    {
        if (startLocation == null)
        {
            return 0;
        }

        return InteractionUtils.distanceTo2DHypotenuse(startLocation, client.getLocalPlayer().getWorldLocation());
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (config.autocombatHotkey().matches(e))
        {
            clientThread.invoke(() -> {
                lastTickActive = client.getTickCount();
                autoCombatRunning = !autoCombatRunning;
                expectedLootLocations.clear();
                npcsKilled.clear();

                if (autoCombatRunning)
                {
                    taskEnded = false;
                    startLocation = client.getLocalPlayer().getWorldLocation();
                }
                else
                {
                    startLocation = null;
                }
            });
        }
    }

    private boolean isStackable(int id)
    {
        ItemComposition composition = client.getItemDefinition(id);
        return composition.isStackable();
    }

    private boolean canBuryOrScatter(int id)
    {
        ItemComposition composition = client.getItemDefinition(id);
        return Arrays.asList(composition.getInventoryActions()).contains("Bury") || Arrays.asList(composition.getInventoryActions()).contains("Scatter");
    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }
}
