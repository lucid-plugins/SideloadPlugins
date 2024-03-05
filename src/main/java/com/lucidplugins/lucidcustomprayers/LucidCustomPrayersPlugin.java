package com.lucidplugins.lucidcustomprayers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provides;
import com.lucidplugins.api.item.SlottedItem;
import com.lucidplugins.api.utils.CombatUtils;
import com.lucidplugins.api.utils.EquipmentUtils;
import com.lucidplugins.api.utils.MessageUtils;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#32CD32\">Lucid </font>Custom Prayers</html>",
        description = "Set up auto prayers based on various event IDs",
        enabledByDefault = false,
        tags = {"prayer", "swap"}
)
public class LucidCustomPrayersPlugin extends Plugin implements KeyListener
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidCustomPrayersConfig config;

    @Inject ConfigManager configManager;

    @Inject
    private KeyManager keyManager;

    private Map<EventType, List<CustomPrayer>> eventMap = new HashMap<>();

    private List<Projectile> validProjectiles = new ArrayList<>();

    private List<ScheduledPrayer> scheduledPrayers = new ArrayList<>();

    private List<Integer> animationsThisTick = new ArrayList<>();

    private List<Integer> npcsSpawnedThisTick = new ArrayList<>();

    private List<Integer> npcsDespawnedThisTick = new ArrayList<>();

    private List<Integer> npcsChangedThisTick = new ArrayList<>();

    private List<Integer> projectilesSpawnedThisTick = new ArrayList<>();

    private List<Integer> graphicsCreatedThisTick = new ArrayList<>();

    private List<Integer> gameObjectsSpawnedThisTick = new ArrayList<>();

    private List<Integer> npcsInteractingWithYouThisTick = new ArrayList<>();

    private List<Integer> npcsYouInteractedWithThisTick = new ArrayList<>();

    private List<String> lastEquipmentList = new ArrayList<>();

    public static final File PRESET_DIR = new File(RUNELITE_DIR, "lucid-custom-prayers");

    public static final String FILENAME_SPECIAL_CHAR_REGEX = "[^a-zA-Z\\d:]";

    public final GsonBuilder builder = new GsonBuilder()
            .setPrettyPrinting();
    public final Gson gson = builder.create();

    public Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private static boolean oneTickFlicking = false;

    private static boolean disableQuickPrayers = false;

    @Provides
    LucidCustomPrayersConfig getConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(LucidCustomPrayersConfig.class);
    }

    @Override
    protected void startUp()
    {

        keyManager.registerKeyListener(this);
        parsePrayers();
    }

    @Override
    protected void shutDown()
    {
        keyManager.unregisterKeyListener(this);
    }

    @Subscribe
    private void onAnimationChanged(final AnimationChanged event)
    {
        if (event.getActor() == null)
        {
            return;
        }

        int animId = event.getActor().getAnimation();

        if (!animationsThisTick.contains(animId) || config.allowDuplicateAnimationEvents())
        {
            if (event.getActor() instanceof NPC)
            {
                final NPC npc = (NPC) event.getActor();
                eventFired(EventType.ANIMATION_CHANGED, animId, npc.getInteracting() == client.getLocalPlayer());
            }
            else
            {
                eventFired(EventType.ANIMATION_CHANGED, animId, event.getActor() == client.getLocalPlayer());
            }

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
            eventFired(EventType.NPC_SPAWNED, npcId, false);
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
            eventFired(EventType.NPC_DESPAWNED, npcId, false);
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
            eventFired(EventType.NPC_CHANGED, npcId, event.getNpc().getInteracting() == client.getLocalPlayer());
            npcsChangedThisTick.add(npcId);
        }
    }

    @Subscribe
    private void onProjectileMoved(final ProjectileMoved event)
    {
        if (validProjectiles.contains(event.getProjectile()))
        {
            return;
        }

        validProjectiles.add(event.getProjectile());

        int projectileId = event.getProjectile().getId();
        if (!projectilesSpawnedThisTick.contains(projectileId) || config.allowDuplicateProjectileEvents())
        {
            eventFired(EventType.PROJECTILE_SPAWNED, projectileId, event.getProjectile().getTarget().equals(client.getLocalPlayer().getLocalLocation()) || event.getProjectile().getInteracting() == client.getLocalPlayer());
            projectilesSpawnedThisTick.add(projectileId);
        }
    }

    @Subscribe
    private void onGraphicsObjectCreated(final GraphicsObjectCreated event)
    {
        int graphicsId = event.getGraphicsObject().getId();

        if (!graphicsCreatedThisTick.contains(graphicsId) || config.allowDuplicateGraphicsEvents())
        {
            eventFired(EventType.GRAPHICS_CREATED, graphicsId, event.getGraphicsObject().getLocation().equals(client.getLocalPlayer().getLocalLocation()));
            graphicsCreatedThisTick.add(graphicsId);
        }
    }

    @Subscribe
    private void onGameObjectSpawned(final GameObjectSpawned event)
    {
        int objectId = event.getGameObject().getId();

        if (!gameObjectsSpawnedThisTick.contains(objectId))
        {
            eventFired(EventType.GAME_OBJECT_SPAWNED, objectId, false);
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
            final NPC npc = (NPC) source;
            if (!npcsInteractingWithYouThisTick.contains(npc.getId()))
            {
                eventFired(EventType.OTHER_INTERACT_YOU, npc.getId(), true);
                npcsInteractingWithYouThisTick.add(npc.getId());
            }
        }

        if (source == client.getLocalPlayer() && !(interacting instanceof Player))
        {
            final NPC interactingNpc = (NPC) interacting;
            if (!npcsYouInteractedWithThisTick.contains(interactingNpc.getId()))
            {
                eventFired(EventType.YOU_INTERACT_OTHER, interactingNpc.getId(), interacting.getInteracting() == client.getLocalPlayer());
                npcsYouInteractedWithThisTick.add(interactingNpc.getId());
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

        getEquipmentChanges();

        if (oneTickFlicking)
        {
            if (CombatUtils.isQuickPrayersEnabled(client))
            {
                CombatUtils.toggleQuickPrayers(client);
                CombatUtils.toggleQuickPrayers(client);
            }
            else
            {
                CombatUtils.toggleQuickPrayers(client);
            }
        }
        else
        {
            if (disableQuickPrayers && CombatUtils.isQuickPrayersEnabled(client))
            {
                CombatUtils.toggleQuickPrayers(client);
                disableQuickPrayers = false;
            }
        }

        for (ScheduledPrayer prayer : scheduledPrayers)
        {
            if (client.getTickCount() == prayer.getActivationTick())
            {
                activatePrayer(client, prayer.getPrayer(), prayer.isToggle());
            }
        }

        scheduledPrayers.removeIf(prayer -> prayer.getActivationTick() <= client.getTickCount() - 1);

        animationsThisTick.clear();
        npcsSpawnedThisTick.clear();
        npcsDespawnedThisTick.clear();
        npcsChangedThisTick.clear();
        projectilesSpawnedThisTick.clear();
        graphicsCreatedThisTick.clear();
        gameObjectsSpawnedThisTick.clear();
        npcsInteractingWithYouThisTick.clear();
        npcsYouInteractedWithThisTick.clear();

        validProjectiles.removeIf(proj -> proj.getRemainingCycles() < 1);
    }

    private void getEquipmentChanges()
    {
        Widget bankWidget = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankWidget != null && !bankWidget.isSelfHidden())
        {
            return;
        }

        final List<SlottedItem> equippedItems = EquipmentUtils.getAll();
        final List<String> itemsMapped = equippedItems.stream().map(item -> client.getItemDefinition(item.getItem().getId()).getName()).collect(Collectors.toList());

        if (!listsMatch(itemsMapped, lastEquipmentList))
        {
            for (SlottedItem slottedItem : equippedItems)
            {
                String name = client.getItemDefinition(slottedItem.getItem().getId()).getName();
                if (!lastEquipmentList.contains(name))
                {
                    eventFired(EventType.ITEM_EQUIPPED, slottedItem.getItem().getId(), false);
                }
            }
            lastEquipmentList.clear();
            lastEquipmentList.addAll(itemsMapped);
        }
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
        boolean ignoreNonTargetEvent = false;

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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents1();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents2();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents3();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents4();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents5();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents6();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents7();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents8();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents9();
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
                    ignoreNonTargetEvent = config.ignoreNonTargetEvents10();
                }
                break;
        }

        if (ids.isEmpty() || prayChoice == null)
        {
            return;
        }

        populatePrayersList(ids, delays, prayChoice, type, toggle, ignoreNonTargetEvent);
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

    private void populatePrayersList(List<Integer> ids, List<Integer> delays, Prayer prayer, EventType type, boolean toggle, boolean ignoreNonTargetEvent)
    {
        if (!delays.isEmpty() && delays.size() != ids.size())
        {
            if (client.getGameState() == GameState.LOGGED_IN)
            {
                clientThread.invoke(() -> MessageUtils.addMessage(client, "If delays are specified, delays and ids list must be the same length!"));
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
                prayerList.add(new CustomPrayer(ids.get(i), prayer, delays.get(i), toggle, ignoreNonTargetEvent));
            }
            else
            {
                prayerList.add(new CustomPrayer(ids.get(i), prayer, 0, toggle, ignoreNonTargetEvent));
            }
        }

        eventMap.put(type, prayerList);
    }

    private static void activatePrayer(Client client, Prayer prayer, boolean toggle)
    {
        boolean useQuickPrayers = false;

        if (prayer == Prayer.THICK_SKIN)
        {
            useQuickPrayers = true;
        }

        if (prayer == Prayer.BURST_OF_STRENGTH)
        {
            if (toggle)
            {
                oneTickFlicking = !oneTickFlicking;
                if (!oneTickFlicking)
                {
                    disableQuickPrayers = true;
                }
            }
            else
            {
                oneTickFlicking = true;
            }
            return;
        }

        if (prayer == Prayer.CLARITY_OF_THOUGHT)
        {
            oneTickFlicking = false;
            disableQuickPrayers = true;
            return;
        }

        if (toggle)
        {
            if (useQuickPrayers)
            {
                CombatUtils.toggleQuickPrayers(client);
            }
            else
            {
                CombatUtils.togglePrayer(client, prayer);
            }

        }
        else
        {
            if (useQuickPrayers)
            {
                CombatUtils.activateQuickPrayers(client);
            }
            else
            {
                CombatUtils.activatePrayer(client, prayer);
            }
        }
    }

    private void eventFired(EventType type, int id, boolean isTargetingPlayer)
    {
        if (config.debugMode() && isEventDebugged(type))
        {
            if ((config.hideNonTargetEventsDebug() && isTargetingPlayer) || !config.hideNonTargetEventsDebug())
            {
                MessageUtils.addMessage(client, "Event Type: " + type.name() + ",  ID: " + id + ", Tick: " + client.getTickCount() + ", Targeting player: " + isTargetingPlayer);
            }
        }

        List<CustomPrayer> prayers = eventMap.get(type);
        if (prayers == null || prayers.isEmpty())
        {
            return;
        }

        for (final CustomPrayer prayer : prayers)
        {
            if (prayer.getActivationId() == id)
            {
                if (prayer.isIgnoreNonTargetEvent())
                {
                    if (!isTargetingPlayer)
                    {
                        continue;
                    }
                }

                scheduledPrayers.add(new ScheduledPrayer(prayer.getPrayerToActivate(), client.getTickCount() + prayer.getTickDelay(), prayer.isToggle()));
            }
        }
    }

    public boolean listsMatch(List<String> list1, List<String> list2)
    {
        if (list1.size() != list2.size())
        {
            return false;
        }

        List<String> list2Copy = Lists.newArrayList(list2);
        for (String element : list1)
        {
            if (!list2Copy.remove(element))
            {
                return false;
            }
        }

        return list2Copy.isEmpty();
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

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (config.toggle1tickQuickPrayersHotkey().matches(e))
        {
            oneTickFlicking = !oneTickFlicking;
            if (!oneTickFlicking)
            {
                disableQuickPrayers = true;
            }
        }

        if (config.loadPresetHotkey().matches(e))
        {
            loadPreset();
        }

        if (config.savePresetHotkey().matches(e))
        {
            savePreset();
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }

    private void savePreset()
    {
        String presetName = config.presetName();
        String presetNameFormatted = presetName.replaceAll(FILENAME_SPECIAL_CHAR_REGEX, "").replaceAll(" ", "_").toLowerCase();

        if (presetNameFormatted.isEmpty())
        {
            return;
        }

        ExportableConfig exportableConfig = new ExportableConfig();

        exportableConfig.setPrayer(0, config.activated1(), config.pray1Ids(), config.pray1delays(), config.pray1choice(), config.eventType1(), config.toggle1(), config.ignoreNonTargetEvents1());
        exportableConfig.setPrayer(1, config.activated2(), config.pray2Ids(), config.pray2delays(), config.pray2choice(), config.eventType2(), config.toggle2(), config.ignoreNonTargetEvents2());
        exportableConfig.setPrayer(2, config.activated3(), config.pray3Ids(), config.pray3delays(), config.pray3choice(), config.eventType3(), config.toggle3(), config.ignoreNonTargetEvents3());
        exportableConfig.setPrayer(3, config.activated4(), config.pray4Ids(), config.pray4delays(), config.pray4choice(), config.eventType4(), config.toggle4(), config.ignoreNonTargetEvents4());
        exportableConfig.setPrayer(4, config.activated5(), config.pray5Ids(), config.pray5delays(), config.pray5choice(), config.eventType5(), config.toggle5(), config.ignoreNonTargetEvents5());
        exportableConfig.setPrayer(5, config.activated6(), config.pray6Ids(), config.pray6delays(), config.pray6choice(), config.eventType6(), config.toggle6(), config.ignoreNonTargetEvents6());
        exportableConfig.setPrayer(6, config.activated7(), config.pray7Ids(), config.pray7delays(), config.pray7choice(), config.eventType7(), config.toggle7(), config.ignoreNonTargetEvents7());
        exportableConfig.setPrayer(7, config.activated8(), config.pray8Ids(), config.pray8delays(), config.pray8choice(), config.eventType8(), config.toggle8(), config.ignoreNonTargetEvents8());
        exportableConfig.setPrayer(8, config.activated9(), config.pray9Ids(), config.pray9delays(), config.pray9choice(), config.eventType9(), config.toggle9(), config.ignoreNonTargetEvents9());
        exportableConfig.setPrayer(9, config.activated10(), config.pray10Ids(), config.pray10delays(), config.pray10choice(), config.eventType10(), config.toggle10(), config.ignoreNonTargetEvents10());

        if (!PRESET_DIR.exists())
        {
            PRESET_DIR.mkdirs();
        }

        File saveFile = new File(PRESET_DIR, presetNameFormatted + ".json");
        try (FileWriter fw = new FileWriter(saveFile))
        {
            fw.write(gson.toJson(exportableConfig));
            fw.close();
            JOptionPane.showMessageDialog(null, "Successfully saved preset '" + presetNameFormatted + "' at " + saveFile.getAbsolutePath(), "Preset Save Success", INFORMATION_MESSAGE);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Save Preset Error", WARNING_MESSAGE);
            log.error(e.getMessage());
        }
    }

    private void loadPreset()
    {
        String presetName = config.presetName();
        String presetNameFormatted = presetName.replaceAll(FILENAME_SPECIAL_CHAR_REGEX, "").replaceAll(" ", "_").toLowerCase();

        if (presetNameFormatted.isEmpty())
        {
            return;
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(PRESET_DIR + "/" + presetNameFormatted + ".json"));
            ExportableConfig loadedConfig = gson.fromJson(br, ExportableConfig.class);
            br.close();
            if (loadedConfig != null)
            {
                log.info("Loaded preset: " + presetNameFormatted);
            }

            for (int i = 0; i < 10; i++)
            {
                configManager.setConfiguration("lucid-custom-prayers", "activated" + (i + 1), loadedConfig.getPrayerEnabled()[i]);
                configManager.setConfiguration("lucid-custom-prayers", "pray" + (i + 1) + "Ids", loadedConfig.getPrayerIds()[i]);
                configManager.setConfiguration("lucid-custom-prayers", "pray" + (i + 1) + "delays", loadedConfig.getPrayerDelays()[i]);
                configManager.setConfiguration("lucid-custom-prayers", "pray" + (i + 1) + "choice", loadedConfig.getPrayChoice()[i]);
                configManager.setConfiguration("lucid-custom-prayers", "eventType" + (i + 1), loadedConfig.getEventType()[i]);
                configManager.setConfiguration("lucid-custom-prayers", "toggle" + (i + 1), loadedConfig.getToggle()[i]);
                configManager.setConfiguration("lucid-custom-prayers", "ignoreNonTargetEvents" + (i + 1), loadedConfig.getIgnoreNonTargetEvents()[i]);
            }

            JOptionPane.showMessageDialog(null, "Successfully loaded preset '" + presetNameFormatted + "'", "Preset Load Success", INFORMATION_MESSAGE);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Preset Load Error", WARNING_MESSAGE);
            log.error(e.getMessage());
        }

    }
}