package com.lucidplugins.api.utils;

import com.example.InteractionApi.TileObjectInteraction;
import net.runelite.api.*;

import java.util.Arrays;

public class GameObjectUtils
{
    public static TileObject getFirstTileObjectAt(Tile tile, int... ids)
    {
        return Arrays.stream(tile.getGameObjects()).filter(gameObject -> gameObject != null && Arrays.asList(ids).contains(gameObject.getId())).findFirst().orElse(null);
    }

    public static void interact(GameObject object, String action)
    {
        TileObjectInteraction.interact(object, action);
    }

    public static void interact(TileObject object, String action)
    {
        TileObjectInteraction.interact(object, action);
    }

    public static void interact(WallObject object, String action)
    {
        TileObjectInteraction.interact(object, action);
    }

    public static boolean hasAction(Client client, int objectId, String action)
    {
        if (client == null)
        {
            return false;
        }

        ObjectComposition composition = client.getObjectDefinition(objectId);

        if (composition == null)
        {
            return false;
        }

        return Arrays.stream(composition.getActions()).anyMatch(s -> s != null && s.equalsIgnoreCase(action));
    }
}
