package com.lucidplugins.lucidcombat;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.google.inject.Provides;
import com.lucidplugins.api.item.SlottedItem;
import com.lucidplugins.api.utils.*;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;


@Extension
@PluginDescriptor(name = "Lucid Combat", description = "Helps with Combat related stuff", enabledByDefault = false)
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
public class LucidCombatPlugin extends Plugin implements KeyListener
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidCombatOverlay overlay;

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

        nextHpToRestoreAt = Math.max(1, config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0));
        nextPrayerLevelToRestoreAt = Math.max(1, config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0));
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
        if (event.getType() == ChatMessageType.ENGINE && event.getMessage().contains("return to a Slayer master"))
        {
            if (config.stopOnTaskCompletion() && autoCombatRunning)
            {
                secondaryStatus = "Slayer Task Done";
                autoCombatRunning = false;
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick tick)
    {
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
        if (client.getTickCount() - lastTickActive > 100)
        {
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

            if (lastTarget != null)
            {
                if (lastTarget instanceof Player)
                {
                    PlayerUtils.interactPlayer(lastTarget.getName(), "Attack");
                }
                else if (lastTarget instanceof NPC)
                {
                    NpcUtils.interact((NPC)lastTarget, "Attack");
                }

                lastTarget = null;
            }
        }


        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleAutoCombat();
        }

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

    private boolean handleAutoCombat()
    {
        if (!autoCombatRunning)
        {
            return false;
        }

        if (!canReact())
        {
            return false;
        }

        if (targetDeadOrNoTarget())
        {
            NPC target = getEligibleTarget();
            if (target != null)
            {
                NpcUtils.interact(target, "Attack");
                nextReactionTick = client.getTickCount() + getReaction();
                secondaryStatus = "Attacking " + target.getName();
                return true;
            }
            else
            {
                secondaryStatus = "Nothing to murder";
                nextReactionTick = client.getTickCount() + getReaction();
                return false;
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

        int randomMinDelay = randomStyle().getLowestDelay();
        int randomMaxDelay = randomStyle().getHighestDelay();

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
            for(String npcName : config.npcToFight().split(","))
            {
                if (npc.getName() != null && npc.getName().contains(npcName))
                {
                    nameContains = true;
                    break;
                }
            }

            int ratio = npc.getHealthRatio();
            int scale = npc.getHealthScale();

            double targetHpPercent = Math.floor((double) ratio  / (double) scale * 100);
            return nameContains && ((npc.getInteracting() == null || npc.getInteracting() == client.getLocalPlayer()) && ratio != 0) &&
                    Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
                    InteractionUtils.isWalkable(npc.getWorldLocation());
        });
    }

    private boolean targetDeadOrNoTarget()
    {
        if (client.getLocalPlayer().getInteracting() == null)
        {
            return true;
        }

        if (client.getLocalPlayer().getInteracting() instanceof NPC)
        {
            NPC npcTarget = (NPC) client.getLocalPlayer().getInteracting();
            int ratio = npcTarget.getHealthRatio();
            int scale = npcTarget.getHealthScale();

            double targetHpPercent = Math.floor((double) ratio  / (double) scale * 100);
            return ratio == 0;
        }

        return false;
    }

    private void updatePluginVars()
    {
        if (client.getLocalPlayer().getAnimation() != -1)
        {
            lastTickActive = client.getTickCount();
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
        } else if (divineCombatPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
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

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (config.autocombatHotkey().matches(e))
        {
            autoCombatRunning = !autoCombatRunning;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }
}
