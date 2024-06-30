package com.lucidplugins.oneclickagility;

import com.example.EthanApiPlugin.Collections.ETileItem;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.WidgetInfoExtended;
import com.example.Packets.MovementPackets;
import com.google.inject.Provides;
import com.lucidplugins.api.utils.InteractionUtils;
import com.lucidplugins.api.utils.InventoryUtils;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import static net.runelite.api.ItemID.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.GameEventManager;
import org.pf4j.Extension;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#32CD32\">Sundar </font>One Click Agility</html>",
        description = "Reclined gaming",
        tags = {"sundar", "pajeet", "agility", "one click", "skilling"},
        enabledByDefault = false
)

@Slf4j
@Singleton
public class OneClickAgilityPlugin extends Plugin
{
    @Inject
    private EventBus eventBus;

    @Inject
    private Client client;

    @Inject
    GameEventManager gameEventManager;

    @Inject
    ItemManager itemManager;

    @Inject
    private OneClickAgilityConfig config;

    @Inject
    private ConfigManager configManager;

    @Provides
    OneClickAgilityConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickAgilityConfig.class);
    }

    private static final int HIGH_ALCH_GRAPHIC = 113;
    private static final Set<Integer> PORTAL_IDS = Set.of(36241,36242,36243,36244,36245,36246);
    private static final Set<Integer> SUMMER_PIE_ID = Set.of(SUMMER_PIE,HALF_A_SUMMER_PIE);
    private static final Set<Integer> STAMINA_POTIONS = Set.of(STAMINA_POTION1, STAMINA_POTION2, STAMINA_POTION3, STAMINA_POTION4, STAMINA_MIX1, STAMINA_MIX2);
    private static final Set<Integer> AGILITY_POTIONS = Set.of(AGILITY_POTION1, AGILITY_POTION2, AGILITY_POTION3, AGILITY_POTION4, AGILITY_MIX1, AGILITY_MIX2);
    private static final WorldPoint SEERS_END = new WorldPoint(2704,3464,0);
    private static final WorldPoint PYRAMID_TOP_RIGHT = new WorldPoint(3043,4697,3);
    private static final WorldPoint PYRAMID_TOP_LEFT = new WorldPoint(3042,4697,3);

    private final ArrayList<Tile> marks = new ArrayList<>();
    private final ArrayList<GameObject> portals = new ArrayList<>();
    private DecorativeObject pyramidTopObstacle;
    private GameObject pyramidTop;
    private Course course;
    private boolean hasAlched;

    private int lastClickTick = 0;

    @Override
    protected void startUp()
    {
        course = CourseFactory.build(config.courseSelection());
    }

    @Override
    protected void shutDown()
    {

    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("oneclickagility"))
        {
            course = CourseFactory.build(config.courseSelection());
            gameEventManager.simulateGameEvents(this);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if(event.getMenuOption().equals("<col=00ff00>One Click Agility"))
        {
            handleClick(event);
            log.debug(event.getMenuOption() + ", "
                    + event.getMenuTarget() + ", "
                    + event.getId() + ", "
                    + (event.getMenuAction().name() != null ? event.getMenuAction().name() + ",  " : "null, ")
                    + event.getParam0() + ", "
                    + event.getParam1());
        }
        else if(event.getMenuOption().equals("One Click Agility"))
        {
            event.consume();
        }
    }

    @Subscribe (priority = 1)
    private void onClientTick(ClientTick event)
    {
        if(client.getLocalPlayer() == null
              || client.getGameState() != GameState.LOGGED_IN
              || client.isMenuOpen()
              || client.getWidget(378,78) != null)//login button
        {
            return;
        }
        String text;

        if(course.getCurrentObstacleArea(client.getLocalPlayer()) == null)
        {
            if (config.consumeMisclicks())
            {
                text = "One Click Agility";
            }
            else
            {
                return;
            }
        }
        else
        {
            text =  "<col=00ff00>One Click Agility";
        }

        MenuEntry entry = client.createMenuEntry(-1);
        entry.setOption(text);
        entry.setTarget("");
        entry.setType(MenuAction.UNKNOWN);
        entry.setParam0(0);
        entry.setParam1(0);
        entry.setIdentifier(0);
        entry.setWorldViewId(client.getTopLevelWorldView().getId());
        entry.setForceLeftClick(true);
        client.setMenuEntries(new MenuEntry[] {entry});
    }

    @Subscribe
    private void onGraphicChanged(GraphicChanged event)
    {
        if (event.getActor().equals(client.getLocalPlayer()) && client.getLocalPlayer().hasSpotAnim(HIGH_ALCH_GRAPHIC))
        {
            hasAlched = true;
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        if(event.getGameObject() == null)
        {
            return;
        }

        if(event.getGameObject().getId() == 10869)
        {
            pyramidTop = event.getGameObject();
        }
        if (PORTAL_IDS.contains(event.getGameObject().getId()))
        {
            portals.add(event.getGameObject());
            return;
        }

        addToCourse(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        if(event.getGameObject() == null)
        {
            return;
        }
        if (PORTAL_IDS.contains(event.getGameObject().getId()))
        {
            portals.remove(event.getGameObject());
            return;
        }
        if(event.getGameObject().getId() == 10869)
        {
            pyramidTop = null;
        }
        removeFromCourse(event.getGameObject());
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        addToCourse(event.getWallObject());
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event)
    {
        removeFromCourse(event.getWallObject());
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
    {
        if(event.getDecorativeObject().getId() == 10851
                && (pyramidTopObstacle == null || pyramidTopObstacle.getY() > event.getDecorativeObject().getY()))
        {
            pyramidTopObstacle = event.getDecorativeObject();
            return;
        }

        addToCourse(event.getDecorativeObject());
    }

    @Subscribe
    public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
    {
        if(event.getDecorativeObject().getId() == 10851 && event.getDecorativeObject() == pyramidTopObstacle)
        {
            pyramidTopObstacle = null;
            return;
        }

        removeFromCourse(event.getDecorativeObject());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event)
    {
        addToCourse(event.getGroundObject());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned event)
    {
        removeFromCourse(event.getGroundObject());
    }

    private void addToCourse(TileObject tileObject)
    {
        if (course.obstacleIDs.contains(tileObject.getId()))
        {
            course.addObstacle(tileObject);
        }
    }

    private void removeFromCourse(TileObject tileObject)
    {
        if (course.obstacleIDs.contains(tileObject.getId()))
        {
            course.removeObstacle(tileObject);
        }
    }

    private void handleClick(MenuOptionClicked event)
    {
        if (client.getTickCount() - lastClickTick < 3)
        {
            event.consume();
            return;
        }

        if (shouldStam())
        {
            InventoryUtils.itemInteract(getLowestStamina().getId(), "Drink");
            lastClickTick = client.getTickCount();
            return;
        }

        if(shouldBoost())
        {
            Item halfPie = InventoryUtils.getFirstItem("Half a summer pie");
            Item pie = InventoryUtils.getFirstItem("Summer pie");
            if (halfPie != null)
            {
                InventoryUtils.itemInteract(halfPie.getId(), "Eat");
            }
            else if (pie != null)
            {
                InventoryUtils.itemInteract(pie.getId(), "Eat");
            }

            lastClickTick = client.getTickCount();
            return;
        }

        if(shouldSeersTele())
        {
            lastClickTick = client.getTickCount();
            EthanApiPlugin.invoke(-1, WidgetInfoExtended.SPELL_CAMELOT_TELEPORT.getId(), MenuAction.CC_OP.getId(), 2, -1, client.getTopLevelWorldView().getId(), "Seers'", "Camelot Teleport", -1, -1);
            return;
        }

        if(atPyramidTop())
        {
            lastClickTick = client.getTickCount();
            EthanApiPlugin.invoke(pyramidTopObstacle.getLocalLocation().getSceneX(), pyramidTopObstacle.getLocalLocation().getSceneY(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), pyramidTopObstacle.getId(), -1, client.getTopLevelWorldView().getId(), "Climb", "Climbing rocks", -1, -1);
            return;
        }

        ObstacleArea obstacleArea = course.getCurrentObstacleArea(client.getLocalPlayer());
        if (obstacleArea == null)
        {
            return;
        }

        ETileItem mog = InteractionUtils.nearestTileItem(item -> item.getTileItem().getId() == MARK_OF_GRACE).orElse(null);
        if (config.pickUpMarks() && mog != null && obstacleArea.containsObject(mog.getLocation()))
        {
            lastClickTick = client.getTickCount();
            InteractionUtils.interactWithTileItem(mog, "Take");
            return;
        }

        if (!portals.isEmpty())
        {
            for(GameObject portal:portals)
            {
                if (obstacleArea.containsObject(portal) && portal.getClickbox() != null)
                {
                    lastClickTick = client.getTickCount();
                    EthanApiPlugin.invoke(portal.getLocalLocation().getSceneX(), portal.getLocalLocation().getSceneY(), MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), portal.getId(), -1, client.getTopLevelWorldView().getId(), "Travel", "Portal", -1, -1);
                    return;
                }
            }
        }

        if(shouldConsume())
        {
            event.consume();
            return;
        }

        MenuEntry obstacleEntry = obstacleArea.createMenuEntry(client);
        if (obstacleEntry != null)
        {
            lastClickTick = client.getTickCount();
            EthanApiPlugin.invoke(obstacleEntry.getParam0(), obstacleEntry.getParam1(), obstacleEntry.getType().getId(), obstacleEntry.getIdentifier(), -1, client.getTopLevelWorldView().getId(), obstacleEntry.getOption(), obstacleEntry.getTarget(), -1, -1);
        }

        if (hasAlched)
        {
            hasAlched = false;
        }

        if (event.getMenuAction() == MenuAction.WALK)
        {
            event.consume();
            LocalPoint point = LocalPoint.fromWorld(client.getTopLevelWorldView(), new WorldPoint(event.getParam0(), event.getParam1(), client.getTopLevelWorldView().getPlane()));
            if (point != null)
            {
                lastClickTick = client.getTickCount();
                MovementPackets.queueMovement(WorldPoint.fromLocal(client, point));
            }
        }
    }

    private boolean shouldConsume()
    {
        if (!config.consumeMisclicks() || hasAlched)
            return false;
        return (client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation()
                || client.getLocalPlayer().getAnimation() != -1);
    }

    private boolean shouldStam()
    {
        return config.useStam()
                && client.getVarbitValue(25) == 0
                && client.getEnergy() < 80
                && getLowestStamina() != null;
    }

    private Item getLowestStamina()
    {
        Item stam1 = InventoryUtils.getFirstItem("Stamina potion(1)");
        Item stam2 = InventoryUtils.getFirstItem("Stamina potion(2)");
        Item stam3 = InventoryUtils.getFirstItem("Stamina potion(3)");
        Item stam4 = InventoryUtils.getFirstItem("Stamina potion(4)");

        if (stam1 != null)
        {
            return stam1;
        }
        else if (stam2 != null)
        {
            return stam2;
        }
        else if (stam3 != null)
        {
            return stam3;
        }
        else
        {
            return stam4;
        }
    }

    private boolean shouldBoost()
    {
        return config.skillBoost()
                && client.getBoostedSkillLevel(Skill.AGILITY)-client.getRealSkillLevel(Skill.AGILITY) < config.boostAmount()
                && getBoostItem() != null;
    }

    private boolean shouldSeersTele()
    {
        return config.seersTele()
                && config.courseSelection() == AgilityCourse.SEERS_VILLAGE
                && client.getVarbitValue(4070) == 0                         //spellbook varbit
                && client.getLocalPlayer().getWorldLocation().equals(SEERS_END)    //worldpoint of dropdown tile
                && client.getLocalPlayer().getAnimation() != 714;                  //teleportation animation ID
    }

    private boolean atPyramidTop()
    {
        return config.courseSelection() == AgilityCourse.AGILITY_PYRAMID
                && (client.getLocalPlayer().getWorldLocation().equals(PYRAMID_TOP_RIGHT) || client.getLocalPlayer().getWorldLocation().equals(PYRAMID_TOP_LEFT))
                && pyramidTop.getRenderable().getModelHeight() == 309;
    }

    private Widget getBoostItem()
    {
        Set<Integer> items = new HashSet<>();
        items.addAll(SUMMER_PIE_ID);
        if (config.boostAmount() <= 3)
            items.addAll(AGILITY_POTIONS);
        return getItem(items);
    }

    public Widget getItem(Collection<Integer> ids) {
        List<Widget> matches = getItems(ids);
        return matches.size() != 0 ? matches.get(0) : null;
    }

    public ArrayList<Widget> getItems(Collection<Integer> ids)
    {
        client.runScript(6009, 9764864, 28, 1, -1);
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        ArrayList<Widget> matchedItems = new ArrayList<>();

        if (inventoryWidget != null && inventoryWidget.getDynamicChildren() != null)
        {
            Widget[] items = inventoryWidget.getDynamicChildren();
            for(Widget item : items)
            {
                if (ids.contains(item.getItemId()))
                {
                    matchedItems.add(item);
                }
            }
        }
        return matchedItems;
    }
}
