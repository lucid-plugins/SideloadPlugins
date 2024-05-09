package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.ETileItem;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileItems;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.*;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class InteractionUtils
{
    public static boolean isRunEnabled()
    {
        return EthanApiPlugin.getClient().getVarpValue(173) == 1;
    }

    public static void toggleRun()
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, 10485787, -1, -1);
    }

    public static int getRunEnergy()
    {
        return EthanApiPlugin.getClient().getEnergy() / 100;
    }


    public static boolean isWidgetHidden(int parentId, int childId, int grandchildId)
    {
        Widget target = EthanApiPlugin.getClient().getWidget(parentId, childId);
        if (grandchildId != -1)
        {
            if (target == null || target.isSelfHidden())
            {
                return true;
            }

            Widget subTarget = target.getChild(grandchildId);
            if (subTarget != null)
            {
                return subTarget.isSelfHidden();
            }
        }

        if (target != null)
        {
            return target.isSelfHidden();
        }

        return true;
    }

    public static int getWidgetSpriteId(int parentId, int childId)
    {
        return getWidgetSpriteId(parentId, childId, -1);
    }

    public static int getWidgetSpriteId(int parentId, int childId, int grandchildId)
    {
        Widget target = EthanApiPlugin.getClient().getWidget(parentId, childId);
        if (grandchildId != -1)
        {
            if (target == null || target.isSelfHidden())
            {
                return -1;
            }

            Widget subTarget = target.getChild(grandchildId);
            if (subTarget != null)
            {
                return subTarget.getSpriteId();
            }
        }

        if (target != null)
        {
            return target.getSpriteId();
        }

        return -1;
    }

    public static String getWidgetText(int parentId, int childId)
    {
        return getWidgetText(parentId, childId, -1);
    }

    public static String getWidgetText(int parentId, int childId, int grandchildId)
    {
        Widget target = EthanApiPlugin.getClient().getWidget(parentId, childId);
        if (grandchildId != -1)
        {
            if (target == null || target.isSelfHidden())
            {
                return "null";
            }

            Widget subTarget = target.getChild(grandchildId);
            if (subTarget != null)
            {
                return subTarget.getText() != null ? subTarget.getText() : "null";
            }
            else
            {
                return "null";
            }
        }

        if (target != null)
        {
            return target.getText() != null ? target.getText() : "null";
        }

        return "null";
    }

    public static boolean isWidgetHidden(int parentId, int childId)
    {
        return isWidgetHidden(parentId, childId, -1);
    }

    public static void widgetInteract(int parentId, int childId, int grandchildId, String action)
    {
        Widget target = EthanApiPlugin.getClient().getWidget(parentId, childId);
        if (target != null && grandchildId != -1)
        {
            target = target.getChild(grandchildId);
        }

        if (target != null && target.getActions() != null)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(target, action);
        }
    }

    public static void widgetInteract(int parentId, int childId, String action)
    {
        widgetInteract(parentId, childId, -1, action);
    }

    public static void queueResumePause(int parentId, int childId, int subchildId)
    {
        WidgetPackets.queueResumePause(parentId << 16 | childId, subchildId);
    }

    public static void useItemOnWallObject(Item item, TileObject object)
    {
        Optional<Widget> itemWidget = Inventory.search().withId(item.getId()).first();
        itemWidget.ifPresent((iw) -> {
            if (object != null)
            {
                MousePackets.queueClickPacket();
                ObjectPackets.queueWidgetOnTileObject(iw, object);
            }
        });
    }

    public static void useLastIdOnWallObject(int id, TileObject object)
    {
        List<Widget> itemWidgets = Inventory.search().withId(id).result();
        Widget itemWidget = itemWidgets.get(itemWidgets.size() - 1);
        if (object != null)
        {
            MousePackets.queueClickPacket();
            ObjectPackets.queueWidgetOnTileObject(itemWidget, object);
        }
    }

    public static void useItemOnNPC(int id, NPC npc)
    {
        Optional<Widget> widget = Inventory.search().filter(i -> i.getItemId() == id).first();

        widget.ifPresent(value -> useWidgetOnNPC(value, npc));
    }


    public static void useWidgetOnNPC(Widget widget, NPC npc)
    {
        if (widget == null || npc == null)
        {
            return;
        }
            MousePackets.queueClickPacket();
            NPCPackets.queueWidgetOnNPC(npc, widget);
    }

    public static void useWidgetOnPlayer(Widget widget, Player player)
    {
        if (widget == null || player == null)
        {
            return;
        }
            MousePackets.queueClickPacket();
            PlayerPackets.queueWidgetOnPlayer(player, widget);
    }

    public static void useWidgetOnTileObject(Widget widget, TileObject object)
    {
        if (widget == null || object == null)
        {
            return;
        }
        MousePackets.queueClickPacket();
        ObjectPackets.queueWidgetOnTileObject(widget, object);
    }

    public static void useWidgetOnTileItem(Widget widget, ETileItem tileItem)
    {
        if (widget == null || tileItem == null)
        {
            return;
        }

        MousePackets.queueClickPacket();
        TileItemPackets.queueWidgetOnTileItem(tileItem, widget, false);
    }

    public static Widget getItemWidget(Item item)
    {
        return Inventory.search().withId(item.getId()).first().orElse(null);
    }

    public static void useWidgetOnWidget(Widget widget, Widget widget2)
    {
        if (widget == null || widget2 == null)
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetOnWidget(widget, widget2);
    }

    public static boolean isMoving()
    {
        return EthanApiPlugin.getClient().getLocalPlayer().getPoseAnimation() != EthanApiPlugin.getClient().getLocalPlayer().getIdlePoseAnimation();
    }

    public static void walk(WorldPoint point)
    {
        MousePackets.queueClickPacket();
        MovementPackets.queueMovement(point);
    }

    public static WorldPoint getClosestSafeLocation(List<LocalPoint> list)
    {
        List<Tile> safeTiles = getAll(tile ->
                !list.contains(tile.getLocalLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation()) < 6
                        && isWalkable(tile.getWorldLocation())
        );

        Tile closestTile = getClosestTile(safeTiles);

        if (closestTile != null)
        {
            return closestTile.getWorldLocation();
        }

        return null;
    }

    public static WorldPoint getClosestSafeLocationP3Enrage(List<LocalPoint> list)
    {
        List<Tile> safeTiles = getAll(tile ->
                !list.contains(tile.getLocalLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation()) < 6
                        && isWalkable(tile.getWorldLocation())
                        && within2RowsWardens(tile.getWorldLocation())
        );

        Tile closestTile = getClosestTile(safeTiles);

        if (closestTile != null)
        {
            return closestTile.getWorldLocation();
        }

        return null;
    }

    private static boolean within2RowsWardens(WorldPoint point)
    {
        int x = point.getRegionX();
        int y = point.getRegionY();

        return y == 37 && x > 27 && x < 37;
    }

    public static WorldPoint getSafeLocationNorthSouth(List<LocalPoint> list)
    {
        final WorldPoint loc = EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation();
        final WorldPoint north = loc.dy(1);
        final WorldPoint northPlus = loc.dy(2);
        final WorldPoint south = loc.dy(-1);
        final WorldPoint southPlus = loc.dy(-2);

        // If last movement setup isnt available just find the first available instead
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(EthanApiPlugin.getClient(), point).equals(north)) || !EthanApiPlugin.reachableTiles().contains(north))
        {
            return north;
        }
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(EthanApiPlugin.getClient(), point).equals(south)) || !EthanApiPlugin.reachableTiles().contains(south))
        {
            return south;
        }
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(EthanApiPlugin.getClient(), point).equals(northPlus)) || !EthanApiPlugin.reachableTiles().contains(northPlus))
        {
            return northPlus;
        }
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(EthanApiPlugin.getClient(), point).equals(southPlus)) || !EthanApiPlugin.reachableTiles().contains(southPlus))
        {
            return southPlus;
        }
        return null;
    }

    public static WorldPoint getClosestSafeLocationNotUnderNPC(List<LocalPoint> list, NPC target)
    {
        List<Tile> safeTiles = getAll(tile ->
                !list.contains(tile.getLocalLocation()) &&
                        !target.getWorldArea().contains(tile.getWorldLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation()) < 6 &&
                        isWalkable(tile.getWorldLocation()));

        Tile closestTile = getClosestTile(safeTiles);

        if (closestTile != null)
        {
            return closestTile.getWorldLocation();
        }

        return null;
    }

    public static WorldPoint getClosestSafeLocationNotInNPCMeleeDistance(List<LocalPoint> list, NPC target)
    {
        return getClosestSafeLocationNotInNPCMeleeDistance(list, target, 6);
    }

    public static WorldPoint getClosestSafeLocationNotInNPCMeleeDistance(List<LocalPoint> list, NPC target, int maxRange)
    {
        List<Tile> safeTiles = getAll(tile ->
                !list.contains(tile.getLocalLocation()) &&
                        !isNpcInMeleeDistanceToLocation(target, tile.getWorldLocation()) &&
                        !target.getWorldArea().contains(tile.getWorldLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation()) < maxRange &&
                        isWalkable(tile.getWorldLocation())
        );

        Tile closestTile = getClosestTile(safeTiles);

        if (closestTile != null)
        {
            return closestTile.getWorldLocation();
        }

        return null;
    }

    public static WorldPoint getClosestSafeLocationInNPCMeleeDistance(List<LocalPoint> list, NPC target)
    {
        List<Tile> safeTiles = getAll(tile ->
                !list.contains(tile.getLocalLocation()) &&
                        isNpcInMeleeDistanceToLocation(target, tile.getWorldLocation()) &&
                        !target.getWorldArea().contains(tile.getWorldLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation()) < 6 &&
                        isWalkable(tile.getWorldLocation())
        );

        Tile closestTile = getClosestTile(safeTiles);

        if (closestTile != null)
        {
            return closestTile.getWorldLocation();
        }

        return null;
    }

    public static WorldPoint getClosestSafeLocationFiltered(List<LocalPoint> list, Predicate<Tile> filter)
    {
        List<Tile> safeTiles = getAll(
                filter.and(tile -> !list.contains(tile.getLocalLocation()))
        );

        Tile closestTile = getClosestTile(safeTiles);

        if (closestTile != null)
        {
            return closestTile.getWorldLocation();
        }

        return null;
    }

    public static Tile getClosestTile(List<Tile> tiles)
    {
        Tile closestTile = null;

        if (tiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : tiles)
            {
                float testDistance = distanceTo2DHypotenuse(EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                // TODO try if ((int)testDistance < (int)closest)
                if (testDistance < closest)
                {
                    closestTile = closeTile;
                    closest = testDistance;
                }
            }
        }
        return closestTile;
    }

    public static List<Tile> getAll(Predicate<Tile> filter)
    {
        List<Tile> out = new ArrayList<>();

        for (int x = 0; x < Constants.SCENE_SIZE; x++)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; y++)
            {
                Tile tile = EthanApiPlugin.getClient().getScene().getTiles()[EthanApiPlugin.getClient().getPlane()][x][y];
                if (tile != null && filter.test(tile))
                {
                    out.add(tile);
                }
            }
        }

        return out;
    }

    public static boolean isNpcInMeleeDistanceToPlayer(NPC target)
    {
        return target.getWorldArea().isInMeleeDistance(EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation());
    }

    public static boolean isNpcInMeleeDistanceToLocation(NPC target, WorldPoint location)
    {
        return target.getWorldArea().isInMeleeDistance(location);
    }

    public static boolean isWalkable(WorldPoint point)
    {
        return EthanApiPlugin.reachableTiles().contains(point);
    }

    public static int approxDistanceTo(WorldPoint point1, WorldPoint point2)
    {
        return Math.max(Math.abs(point1.getX() - point2.getX()), Math.abs(point1.getY() - point2.getY()));
    }

    public static float distanceTo2DHypotenuse(WorldPoint main, WorldPoint other)
    {
        return (float) Math.hypot((main.getX() - other.getX()), (main.getY() - other.getY()));
    }

    public static float distanceTo2DHypotenuse(WorldPoint main, WorldPoint other, int size1, int size2)
    {
        WorldPoint midMain = main.dx((int) Math.floor((float) size1 / 2)).dy((int) Math.floor((float) size1 / 2));
        WorldPoint midOther = other.dx((int) Math.floor((float) size2 / 2)).dy((int) Math.floor((float) size2 / 2));
        return (float) Math.hypot(midMain.getX() - midOther.getX(), midMain.getY() - midOther.getY());
    }

    public static float distanceTo2DHypotenuse(WorldPoint main, WorldPoint other, int size1X, int size1Y, int size2)
    {
        WorldPoint midMain = main.dx((int) Math.floor((float) size1X / 2)).dy((int) Math.floor((float) size1Y / 2));
        WorldPoint midOther = other.dx((int) Math.floor((float) size2 / 2)).dy((int) Math.floor((float) size2 / 2));
        return (float) Math.hypot(midMain.getX() - midOther.getX(), midMain.getY() - midOther.getY());
    }

    public static WorldPoint getCenterTileFromWorldArea(WorldArea area)
    {
        return new WorldPoint(area.getX() + area.getWidth() / 2, area.getY() + area.getHeight() / 2, area.getPlane());
    }

    public static List<ETileItem> getAllTileItems(Predicate<ETileItem> filter)
    {
        return TileItems.search().filter(filter).result();
    }

    public static Optional<ETileItem> nearestTileItem(Predicate<ETileItem> filter)
    {
        return TileItems.search().filter(filter).nearestToPlayer();
    }

    public static boolean tileItemNameExistsWithinDistance(String name, int distance)
    {
        ETileItem item = TileItems.search().nameContains(name).withinDistance(distance).result().stream().findFirst().orElse(null);
        return item != null;
    }

    public static boolean tileItemIdExistsWithinDistance(int itemId, int distance)
    {
        ETileItem item = TileItems.search().withId(itemId).withinDistance(distance).result().stream().findFirst().orElse(null);
        return item != null;
    }

    public static void interactWithTileItem(int itemId, String action)
    {
        ETileItem item = TileItems.search().withId(itemId).nearestToPlayer().orElse(null);

        if (item != null)
        {
            TileItemPackets.queueTileItemAction(item, false);
        }
    }

    public static void interactWithTileItem(String name, String action)
    {
        ETileItem item = TileItems.search().nameContains(name).nearestToPlayer().orElse(null);

        if (item != null)
        {
            TileItemPackets.queueTileItemAction(item, false);
        }
    }

    public static void interactWithTileItem(ETileItem item, String action)
    {
        if (item != null)
        {
            TileItemPackets.queueTileItemAction(item, false);
        }
    }
}
