package com.lucidplugins.lucidwildytele;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;

public class Util
{
    private static final Polygon NOT_WILDERNESS_BLACK_KNIGHTS = new Polygon( // this is black knights castle
            new int[]{2994, 2995, 2996, 2996, 2994, 2994, 2997, 2998, 2998, 2999, 3000, 3001, 3002, 3003, 3004, 3005, 3005,
                      3005, 3019, 3020, 3022, 3023, 3024, 3025, 3026, 3026, 3027, 3027, 3028, 3028, 3029, 3029, 3030, 3030, 3031,
                      3031, 3032, 3033, 3034, 3035, 3036, 3037, 3037},
            new int[]{3525, 3526, 3527, 3529, 3529, 3534, 3534, 3535, 3536, 3537, 3538, 3539, 3540, 3541, 3542, 3543, 3544,
                      3545, 3545, 3546, 3546, 3545, 3544, 3543, 3543, 3542, 3541, 3540, 3539, 3537, 3536, 3535, 3534, 3533, 3532,
                      3531, 3530, 3529, 3528, 3527, 3526, 3526, 3525},
            43
    );
    private static final Cuboid MAIN_WILDERNESS_CUBOID = new Cuboid(2944, 3525, 0, 3391, 4351, 3);
    private static final Cuboid GOD_WARS_WILDERNESS_CUBOID = new Cuboid(3008, 10112, 0, 3071, 10175, 3);
    private static final Cuboid WILDERNESS_UNDERGROUND_CUBOID = new Cuboid(2944, 9920, 0, 3455, 10879, 3);

    private static final Cuboid MULTI_AREA_WILDY_BOSSES = new Cuboid(3264, 10176, 0, 3455, 10367, 3);

    /**
     * Gets the wilderness level based on a world point
     * Java reimplementation of clientscript 384 [proc,wilderness_level]
     *
     * @param point the point in the world to get the wilderness level for
     * @return the int representing the wilderness level
     */
    public static int getWildernessLevelFrom(WorldPoint point)
    {
        int regionID = point.getRegionID();
        if (regionID == 12700 /* soul wars underground ferox */ ||
                regionID == 12187 /* falador party room museum */)
        {
            return 0;
        }

        switch (regionID)
        {
            case 13215: // vetion
            case 13727: // venenatis
                return 35;
            case 13473: //callisto
                return 40;
            case 7604: // calvarion
            case 7092: // artio
                return 21;
            case 6580: // spindel
                return 29;
        }
        if (MAIN_WILDERNESS_CUBOID.contains(point))
        {
            if (NOT_WILDERNESS_BLACK_KNIGHTS.contains(point.getX(), point.getY()))
            {
                return 0;
            }

            return ((point.getY() - 3520) / 8) + 1; // calc(((coordz(coord) - (55 * 64)) / 8) + 1)
        }
        else if (GOD_WARS_WILDERNESS_CUBOID.contains(point))
        {
            return ((point.getY() - 9920) / 8) - 1; // calc(((coordz(coord) - (155 * 64)) / 8) - 1)
        }
        else if (WILDERNESS_UNDERGROUND_CUBOID.contains(point))
        {
            return ((point.getY() - 9920) / 8) + 1; // calc(((coordz(coord) - (155 * 64)) / 8) + 1)
        }
        else if (MULTI_AREA_WILDY_BOSSES.contains(point))
        {
            return 69; // Don't wanna do the calculations but we can't tele anyways
        }

        return 0;
    }

    /**
     * Determines if another player is attackable based off of wilderness level and combat levels
     *
     * @param client The client of the local player
     * @param player the player to determine attackability
     * @return returns true if the player is attackable, false otherwise
     */
    public static boolean isAttackable(Client client, Player player, boolean ignorePlayerWildy)
    {
        int wildernessLevel = 0;

        if (player == null)
        {
            return false;
        }

        if (WorldType.isPvpWorld(client.getWorldType()))
        {
            wildernessLevel += 15;
        }
        if (client.getVarbitValue(Varbits.IN_WILDERNESS) == 1)
        {
            wildernessLevel += getWildernessLevelFrom(client.getLocalPlayer().getWorldLocation());

            if (getWildernessLevelFrom(player.getWorldLocation()) == 0 && !ignorePlayerWildy)
            {
                wildernessLevel = 0;
            }
        }

        return wildernessLevel != 0 && Math.abs(client.getLocalPlayer().getCombatLevel() - player.getCombatLevel()) <= wildernessLevel;
    }
}
