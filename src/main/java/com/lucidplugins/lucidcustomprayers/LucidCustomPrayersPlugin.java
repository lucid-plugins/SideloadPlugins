package com.lucidplugins.lucidcustomprayers;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.PrayerInteraction;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Lucid Custom Prayers",
        description = "Set up auto prayers based on various event IDs",
        enabledByDefault = false,
        tags = {"prayer", "swap"}
)
@PluginDependency(EthanApiPlugin.class)
@PluginDependency(PacketUtilsPlugin.class)
public class LucidCustomPrayersPlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidCustomPrayersConfig config;

    private Map<EventType, List<CustomPrayer>> eventMap = new HashMap<>();

    private List<ScheduledPrayer> scheduledPrayers = new ArrayList<>();

    private List<Integer> animationsThisTick = new ArrayList<>();

    private List<Integer> npcsSpawnedThisTick = new ArrayList<>();

    private List<Integer> npcsDespawnedThisTick = new ArrayList<>();

    private List<Integer> npcsChangedThisTick = new ArrayList<>();

    private List<Integer> projectilesMovedThisTick = new ArrayList<>();

    private List<Integer> graphicsCreatedThisTick = new ArrayList<>();

    private List<Integer> gameObjectsSpawnedThisTick = new ArrayList<>();

    private List<Integer> npcsInteractingWithYouThisTick = new ArrayList<>();

    private List<Integer> npcsYouInteractedWithThisTick = new ArrayList<>();

    private List<Item> equippedGearLastTick = new ArrayList<>();

    @Provides
    LucidCustomPrayersConfig getConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(LucidCustomPrayersConfig.class);
    }

    @Override
    protected void startUp()
    {
        parsePrayers();
    }

    @Override
    protected void shutDown()
    {

    }

    @Subscribe
    private void onAnimationChanged(final AnimationChanged event)
    {
        if (event.getActor() == null)
        {
            return;
        }

        int animId = event.getActor().getAnimation();

        if (!animationsThisTick.contains(animId))
        {
            eventFired(EventType.ANIMATION_CHANGED, animId);
            animationsThisTick.add(animId);
        }
    }

    @Subscribe
    private void onNpcSpawned(final NpcSpawned event)
    {
        if (event.getNpc() == null)
        {
            return;
        }

        int npcId = event.getNpc().getId();

        if (!npcsSpawnedThisTick.contains(npcId))
        {
            eventFired(EventType.NPC_SPAWNED, npcId);
            npcsSpawnedThisTick.add(npcId);
        }
    }

    @Subscribe
    private void onNpcDespawned(final NpcDespawned event)
    {
        if (event.getNpc() == null)
        {
            return;
        }

        int npcId = event.getNpc().getId();

        if (!npcsDespawnedThisTick.contains(npcId))
        {
            eventFired(EventType.NPC_DESPAWNED, npcId);
            npcsDespawnedThisTick.add(npcId);
        }
    }

    @Subscribe
    private void onNpcChanged(final NpcChanged event)
    {
        if (event.getNpc() == null)
        {
            return;
        }

        int npcId = event.getNpc().getId();

        if (!npcsChangedThisTick.contains(npcId))
        {
            eventFired(EventType.NPC_CHANGED, npcId);
            npcsChangedThisTick.add(npcId);
        }
    }

    @Subscribe
    private void onProjectileMoved(final ProjectileMoved event)
    {
        int projectileId = event.getProjectile().getId();

        if (!projectilesMovedThisTick.contains(projectileId))
        {
            eventFired(EventType.PROJECTILE_SPAWNED, projectileId);
            projectilesMovedThisTick.add(projectileId);
        }
    }

    @Subscribe
    private void onGraphicsObjectCreated(final GraphicsObjectCreated event)
    {
        int graphicsId = event.getGraphicsObject().getId();

        if (!graphicsCreatedThisTick.contains(graphicsId))
        {
            eventFired(EventType.GRAPHICS_CREATED, graphicsId);
            graphicsCreatedThisTick.add(graphicsId);
        }
    }

    @Subscribe
    private void onGameObjectSpawned(final GameObjectSpawned event)
    {
        int objectId = event.getGameObject().getId();

        if (!gameObjectsSpawnedThisTick.contains(objectId))
        {
            eventFired(EventType.GAME_OBJECT_SPAWNED, objectId);
            gameObjectsSpawnedThisTick.add(objectId);
        }
    }

    @Subscribe
    private void onInteractingChanged(final InteractingChanged event)
    {
        Actor source = event.getSource();
        Actor interacting = event.getSource().getInteracting();

        if (interacting == null)
        {
            return;
        }

        if (interacting == client.getLocalPlayer() && !(source instanceof Player))
        {
            NPC npc = (NPC) source;
            if (!npcsInteractingWithYouThisTick.contains(npc.getId()))
            {
                eventFired(EventType.OTHER_INTERACT_YOU, npc.getId());
                npcsInteractingWithYouThisTick.add(npc.getId());
            }
        }

        if (source == client.getLocalPlayer() && !(interacting instanceof Player))
        {
            NPC npc = (NPC) interacting;
            if (!npcsYouInteractedWithThisTick.contains(npc.getId()))
            {
                eventFired(EventType.YOU_INTERACT_OTHER, npc.getId());
                npcsYouInteractedWithThisTick.add(npc.getId());
            }
        }
    }

    @Subscribe
    private void onConfigChanged(final ConfigChanged event)
    {
        if (!event.getGroup().equals("lucid-custom-prayers"))
        {
            return;
        }

        parsePrayers();
    }

    @Subscribe
    private void onGameTick(final GameTick event)
    {

        checkItemEquips();

        for (ScheduledPrayer prayer : scheduledPrayers)
        {
            if (client.getTickCount() == prayer.getActivationTick())
            {
                activatePrayer(prayer.getPrayer(), prayer.isToggle());
            }
        }

        scheduledPrayers.removeIf(prayer -> prayer.getActivationTick() <= client.getTickCount() - 1);

        animationsThisTick.clear();
        npcsSpawnedThisTick.clear();
        npcsDespawnedThisTick.clear();
        npcsChangedThisTick.clear();
        projectilesMovedThisTick.clear();
        graphicsCreatedThisTick.clear();
        gameObjectsSpawnedThisTick.clear();
        npcsInteractingWithYouThisTick.clear();
        npcsYouInteractedWithThisTick.clear();
    }

    private void checkItemEquips()
    {
        final List<Item> equipment = Equipment.search().result().stream().map(equipmentItemWidget -> new Item(equipmentItemWidget.getItemId(), equipmentItemWidget.getItemQuantity())).collect(Collectors.toList());

        for (Item item : equipment)
        {
            if (item == null)
            {
                continue;
            }

            if (equippedGearLastTick.isEmpty() || !equippedGearLastTick.contains(item))
            {
                eventFired(EventType.ITEM_EQUIPPED, item.getId());
            }
        }

        equippedGearLastTick = equipment;
    }


    private void parsePrayers()
    {
        eventMap.clear();

        for (int i = 1; i < 11; i++)
        {
            parsePrayerSlot(i);
        }
    }

    private void parsePrayerSlot(int id)
    {
        List<Integer> ids = List.of();
        List<Integer> delays = List.of();
        Prayer prayChoice = null;
        EventType type = EventType.ANIMATION_CHANGED;
        boolean toggle = false;

        switch (id)
        {
            case 1:
                if (config.activated1())
                {
                    ids = intListFromString(config.pray1Ids());
                    delays = intListFromString(config.pray1delays());
                    prayChoice = config.pray1choice();
                    type = config.eventType1();
                    toggle = config.toggle1();
                }
                break;
            case 2:
                if (config.activated2())
                {
                    ids = intListFromString(config.pray2Ids());
                    delays = intListFromString(config.pray2delays());
                    prayChoice = config.pray2choice();
                    type = config.eventType2();
                    toggle = config.toggle2();
                }
                break;
            case 3:
                if (config.activated3())
                {
                    ids = intListFromString(config.pray3Ids());
                    delays = intListFromString(config.pray3delays());
                    prayChoice = config.pray3choice();
                    type = config.eventType3();
                    toggle = config.toggle3();
                }
                break;
            case 4:
                if (config.activated4())
                {
                    ids = intListFromString(config.pray4Ids());
                    delays = intListFromString(config.pray4delays());
                    prayChoice = config.pray4choice();
                    type = config.eventType4();
                    toggle = config.toggle4();
                }
                break;
            case 5:
                if (config.activated5())
                {
                    ids = intListFromString(config.pray5Ids());
                    delays = intListFromString(config.pray5delays());
                    prayChoice = config.pray5choice();
                    type = config.eventType5();
                    toggle = config.toggle5();
                }
                break;
            case 6:
                if (config.activated6())
                {
                    ids = intListFromString(config.pray6Ids());
                    delays = intListFromString(config.pray6delays());
                    prayChoice = config.pray6choice();
                    type = config.eventType6();
                    toggle = config.toggle6();
                }
                break;
            case 7:
                if (config.activated7())
                {
                    ids = intListFromString(config.pray7Ids());
                    delays = intListFromString(config.pray7delays());
                    prayChoice = config.pray7choice();
                    type = config.eventType7();
                    toggle = config.toggle7();
                }
                break;
            case 8:
                if (config.activated8())
                {
                    ids = intListFromString(config.pray8Ids());
                    delays = intListFromString(config.pray8delays());
                    prayChoice = config.pray8choice();
                    type = config.eventType8();
                    toggle = config.toggle8();
                }
                break;
            case 9:
                if (config.activated9())
                {
                    ids = intListFromString(config.pray9Ids());
                    delays = intListFromString(config.pray9delays());
                    prayChoice = config.pray9choice();
                    type = config.eventType9();
                    toggle = config.toggle9();
                }
                break;
            case 10:
                if (config.activated10())
                {
                    ids = intListFromString(config.pray10Ids());
                    delays = intListFromString(config.pray10delays());
                    prayChoice = config.pray10choice();
                    type = config.eventType10();
                    toggle = config.toggle10();
                }
                break;
        }

        if (ids.isEmpty() || prayChoice == null)
        {
            return;
        }

        populatePrayersList(ids, delays, prayChoice, type, toggle);
    }

    private List<Integer> intListFromString(String stringList)
    {
        List<Integer> ints = new ArrayList<>();
        if (stringList == null || stringList.trim().equals(""))
        {
            return ints;
        }

        if (stringList.contains(","))
        {
            String[] intStrings = stringList.trim().split(",");
            for (String s : intStrings)
            {
                try
                {
                    int anInt = Integer.parseInt(s);
                    ints.add(anInt);
                }
                catch (NumberFormatException e)
                {
                }
            }
        }
        else
        {
            try
            {
                int anInt = Integer.parseInt(stringList);
                ints.add(anInt);
            }
            catch (NumberFormatException e)
            {
            }
        }

        return ints;
    }

    private void populatePrayersList(List<Integer> ids, List<Integer> delays, Prayer prayer, EventType type, boolean toggle)
    {
        if (!delays.isEmpty() && delays.size() != ids.size())
        {
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                addMessage("If delays are specified, delays and ids list must be the same length!");
            }
            delays.clear();
        }

        List<CustomPrayer> prayerList = eventMap.get(type);

        if (prayerList == null)
        {
            prayerList = new ArrayList<>();
        }

        for (int i = 0; i < ids.size(); i++)
        {
            if (!delays.isEmpty())
            {
                prayerList.add(new CustomPrayer(ids.get(i), prayer, delays.get(i), toggle));
            }
            else
            {
                prayerList.add(new CustomPrayer(ids.get(i), prayer, 0, toggle));
            }
        }

        eventMap.put(type, prayerList);
    }

    private void activatePrayer(Prayer prayer, boolean toggle)
    {
        if (client.getBoostedSkillLevel(Skill.PRAYER) == 0)
        {
            return;
        }

        if (client.isPrayerActive(prayer) && !toggle)
        {
            return;
        }

        PrayerInteraction.togglePrayer(prayer);
    }

    private void eventFired(EventType type, int id)
    {
        if (config.debugMode() && isEventDebugged(type))
        {
            addMessage("Event Type: " + type.name() + ",  ID: " + id + ", Tick: " + client.getTickCount());
        }

        List<CustomPrayer> prayers = eventMap.get(type);
        if (prayers == null || prayers.isEmpty())
        {
            return;
        }

        for (CustomPrayer prayer : prayers)
        {
            if (prayer.getActivationId() == id)
            {
                scheduledPrayers.add(new ScheduledPrayer(prayer.getPrayerToActivate(), client.getTickCount() + prayer.getTickDelay(), prayer.isToggle()));
            }
        }
    }

    private boolean isEventDebugged(EventType type)
    {
        switch (type)
        {
            case ANIMATION_CHANGED:
                return config.debugAnimationChanged();
            case NPC_SPAWNED:
                return config.debugNpcSpawned();
            case NPC_DESPAWNED:
                return config.debugNpcDespawned();
            case NPC_CHANGED:
                return config.debugNpcChanged();
            case PROJECTILE_SPAWNED:
                return config.debugProjectileSpawned();
            case GRAPHICS_CREATED:
                return config.debugGraphicsCreated();
            case GAME_OBJECT_SPAWNED:
                return config.debugGameObjectSpawned();
            case OTHER_INTERACT_YOU:
                return config.debugOtherInteractYou();
            case YOU_INTERACT_OTHER:
                return config.debugYouInteractOther();
            case ITEM_EQUIPPED:
                return config.debugItemEquipped();
            default:
                return false;
        }
    }

    private void addMessage(String message)
    {
        if (client == null || !client.isClientThread())
        {
            return;
        }

        client.addChatMessage(ChatMessageType.ENGINE, "", message, "", false);

    }
}