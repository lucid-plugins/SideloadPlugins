package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.ETileItem;
import com.example.EthanApiPlugin.Collections.TileItems;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.TileItemPackets;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InteractionUtils
{

    public static boolean sleep(Client client, long ms)
    {
        if (client.isClientThread())
        {
            return false;
        }
        else
        {
            try
            {
                Thread.sleep(ms);
                return true;
            }
            catch (InterruptedException var3)
            {
                return false;
            }
        }
    }

    public static void walk(WorldPoint point)
    {
        MousePackets.queueClickPacket();
        MovementPackets.queueMovement(point);
    }

    public static WorldPoint getClosestSafeLocation(Client client, List<LocalPoint> list)
    {
        List<Tile> safeTiles = getAll(client, tile -> approxDistanceTo(tile.getWorldLocation(), client.getLocalPlayer().getWorldLocation()) < 6 && isWalkable(tile.getWorldLocation()));
        List<Tile> trueSafeTiles = new ArrayList<>();
        for (Tile t : safeTiles)
        {
            boolean safe = true;
            for (LocalPoint unsafeTile : list)
            {
                if (t.getWorldLocation().equals(WorldPoint.fromLocal(client, unsafeTile)))
                {
                    safe = false;
                }
            }
            if (safe)
            {
                trueSafeTiles.add(t);
            }
        }

        WorldPoint closestTile = null;

        if (trueSafeTiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : trueSafeTiles)
            {
                float testDistance = distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                if (testDistance < closest)
                {
                    closestTile = closeTile.getWorldLocation();
                    closest = testDistance;
                }
            }
        }
        return closestTile;
    }

    public static WorldPoint getClosestSafeLocationP3Enrage(Client client, List<LocalPoint> list)
    {
        List<Tile> safeTiles = getAll(client, tile ->
                approxDistanceTo(tile.getWorldLocation(), client.getLocalPlayer().getWorldLocation()) < 6
                && isWalkable(tile.getWorldLocation())
                && within2RowsWardens(tile.getWorldLocation())
        );
        List<Tile> trueSafeTiles = new ArrayList<>();
        for (Tile t : safeTiles)
        {
            boolean safe = true;
            for (LocalPoint unsafeTile : list)
            {
                if (t.getWorldLocation().equals(WorldPoint.fromLocal(client, unsafeTile)))
                {
                    safe = false;
                }
            }
            if (safe)
            {
                trueSafeTiles.add(t);
            }
        }

        WorldPoint closestTile = null;

        if (trueSafeTiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : trueSafeTiles)
            {
                float testDistance = distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                if (testDistance < closest)
                {
                    closestTile = closeTile.getWorldLocation();
                    closest = testDistance;
                }
            }
        }
        return closestTile;
    }

    private static boolean within2RowsWardens(WorldPoint point)
    {
        int x = point.getRegionX();
        int y = point.getRegionY();

        return y == 37 && x > 27 && x < 37;
    }

    public static List<Tile> getAll(Client client, Predicate<Tile> filter)
    {
        List<Tile> out = new ArrayList<>();

        for (int x = 0; x < Constants.SCENE_SIZE; x++)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; y++)
            {
                Tile tile = client.getScene().getTiles()[client.getPlane()][x][y];
                if (tile != null && filter.test(tile))
                {
                    out.add(tile);
                }
            }
        }

        return out;
    }

    public static WorldPoint getSafeLocationNorthSouth(Client client, List<LocalPoint> list)
    {
        final WorldPoint loc = client.getLocalPlayer().getWorldLocation();
        final WorldPoint north = loc.dy(1);
        final WorldPoint northPlus = loc.dy(2);
        final WorldPoint south = loc.dy(-1);
        final WorldPoint southPlus = loc.dy(-2);

        // If last movement setup isnt available just find the first available instead
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(client, point).equals(north)) || !EthanApiPlugin.reachableTiles().contains(north))
        {
            return north;
        }
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(client, point).equals(south)) || !EthanApiPlugin.reachableTiles().contains(south))
        {
            return south;
        }
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(client, point).equals(northPlus)) || !EthanApiPlugin.reachableTiles().contains(northPlus))
        {
            return northPlus;
        }
        if (list.stream().noneMatch(point -> WorldPoint.fromLocal(client, point).equals(southPlus)) || !EthanApiPlugin.reachableTiles().contains(southPlus))
        {
            return southPlus;
        }
        return null;
    }

    public static WorldPoint getClosestSafeLocationNotUnderNPC(Client client, List<LocalPoint> list, NPC target)
    {
        List<Tile> safeTiles = getAll(client, tile ->
                !target.getWorldArea().contains(tile.getWorldLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), client.getLocalPlayer().getWorldLocation()) < 6 &&
                        isWalkable(tile.getWorldLocation()));
        List<Tile> trueSafeTiles = new ArrayList<>();
        for (Tile t : safeTiles)
        {
            boolean safe = true;
            for (LocalPoint unsafeTile : list)
            {
                if (t.getWorldLocation().equals(WorldPoint.fromLocal(client, unsafeTile)))
                {
                    safe = false;
                }
            }
            if (safe)
            {
                trueSafeTiles.add(t);
            }
        }

        WorldPoint closestTile = null;

        if (trueSafeTiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : trueSafeTiles)
            {
                float testDistance = distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                if (testDistance < closest)
                {
                    closestTile = closeTile.getWorldLocation();
                    closest = testDistance;
                }
            }
        }
        return closestTile;
    }

    public static WorldPoint getClosestSafeLocationNotInNPCMeleeDistance(Client client, List<LocalPoint> list, NPC target)
    {
        List<Tile> safeTiles = getAll(client, tile ->
                approxDistanceTo(getCenterTileFromWorldArea(target.getWorldArea()), tile.getWorldLocation()) > (target.getWorldArea().getWidth() / 2) + 1 &&
                        !target.getWorldArea().contains(tile.getWorldLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), client.getLocalPlayer().getWorldLocation()) < 6 &&
                        isWalkable(tile.getWorldLocation()));

        List<Tile> trueSafeTiles = new ArrayList<>();
        for (Tile t : safeTiles)
        {
            boolean safe = true;
            for (LocalPoint unsafeTile : list)
            {
                if (t.getWorldLocation().equals(WorldPoint.fromLocal(client, unsafeTile)))
                {
                    safe = false;
                }
            }
            if (safe)
            {
                trueSafeTiles.add(t);
            }
        }

        WorldPoint closestTile = null;

        if (trueSafeTiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : trueSafeTiles)
            {
                float testDistance = distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                if (testDistance < closest)
                {
                    closestTile = closeTile.getWorldLocation();
                    closest = testDistance;
                }
            }
        }
        return closestTile;
    }

    public static WorldPoint getClosestSafeLocationInNPCMeleeDistance(Client client, List<LocalPoint> list, NPC target)
    {
        List<Tile> safeTiles = getAll(client, tile ->
                distanceTo2DHypotenuse(getCenterTileFromWorldArea(target.getWorldArea()), tile.getWorldLocation()) >= Math.floor((double)target.getWorldArea().getWidth() / 2) + 1 &&
                        distanceTo2DHypotenuse(getCenterTileFromWorldArea(target.getWorldArea()), tile.getWorldLocation()) <= Math.floor((double)target.getWorldArea().getWidth() / 2) + 1.5 &&
                        !target.getWorldArea().contains(tile.getWorldLocation()) &&
                        approxDistanceTo(tile.getWorldLocation(), client.getLocalPlayer().getWorldLocation()) < 6 &&
                        isWalkable(tile.getWorldLocation()));

        List<Tile> trueSafeTiles = new ArrayList<>();
        for (Tile t : safeTiles)
        {
            boolean safe = true;
            for (LocalPoint unsafeTile : list)
            {
                if (t.getWorldLocation().equals(WorldPoint.fromLocal(client, unsafeTile)))
                {
                    safe = false;
                }
            }
            if (safe)
            {
                trueSafeTiles.add(t);
            }
        }

        WorldPoint closestTile = null;

        if (trueSafeTiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : trueSafeTiles)
            {
                float testDistance = distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                if (testDistance < closest)
                {
                    closestTile = closeTile.getWorldLocation();
                    closest = testDistance;
                }
            }
        }
        return closestTile;
    }

    public static WorldPoint getClosestSafeLocationCustom(Client client, List<LocalPoint> list, Predicate<Tile> filter)
    {
        List<Tile> safeTiles = getAll(client, filter);
        List<Tile> trueSafeTiles = new ArrayList<>();
        for (Tile t : safeTiles)
        {
            boolean safe = true;
            for (LocalPoint unsafeTile : list)
            {
                if (t.getWorldLocation().equals(WorldPoint.fromLocal(client, unsafeTile)))
                {
                    safe = false;
                }
            }
            if (safe)
            {
                trueSafeTiles.add(t);
            }
        }

        WorldPoint closestTile = null;

        if (trueSafeTiles.size() > 0)
        {
            float closest = 999;
            for (Tile closeTile : trueSafeTiles)
            {
                float testDistance = distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), closeTile.getWorldLocation());

                if (testDistance < closest)
                {
                    closestTile = closeTile.getWorldLocation();
                    closest = testDistance;
                }
            }
        }
        return closestTile;
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
        return (float)Math.hypot((double)(main.getX() - other.getX()), (double)(main.getY() - other.getY()));
    }

    public static WorldPoint getCenterTileFromWorldArea(WorldArea area)
    {
        return new WorldPoint(area.getX() + area.getWidth() / 2, area.getY() + area.getHeight() / 2, area.getPlane());
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

        TileItemPackets.queueTileItemAction(item, false);
    }

    public static void interactWithTileItem(String name, String action)
    {
        ETileItem item = TileItems.search().nameContains(name).nearestToPlayer().orElse(null);


        if (item != null)
        {
            TileItemPackets.queueTileItemAction(item, false);
        }
    }
}
