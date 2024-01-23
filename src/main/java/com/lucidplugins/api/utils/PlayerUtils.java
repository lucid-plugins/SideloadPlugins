package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Players;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.PlayerInteractionHelper;
import net.runelite.api.Player;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class PlayerUtils
{
    public static void interactPlayer(String name, String action)
    {
        PlayerInteractionHelper.interact(name, action);
    }

    public static Player getNearest(String name)
    {
        List<Player> players = Players.search().filter(player -> player.getName() != null && player.getName().contains(name)).result();
        Player closestPlayer = null;

        if (players.size() > 0)
        {
            float closest = 999;
            for (Player p : players)
            {
                float testDistance = InteractionUtils.distanceTo2DHypotenuse(EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation(), p.getWorldLocation());

                if (testDistance < closest)
                {
                    closestPlayer = p;
                    closest = testDistance;
                }
            }
        }
        return closestPlayer;
    }

    public static Player getNearest(Predicate<Player> filter)
    {
        List<Player> players = Players.search().filter(filter).result();
        Player closestPlayer = null;

        if (players.size() > 0)
        {
            float closest = 999;
            for (Player p : players)
            {
                float testDistance = InteractionUtils.distanceTo2DHypotenuse(EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation(), p.getWorldLocation());

                if (testDistance < closest)
                {
                    closestPlayer = p;
                    closest = testDistance;
                }
            }
        }

        return closestPlayer;
    }

    private Player nearestTest(List<Player> players)
    {
        return players.size() == 0 ? null : players.stream().min(Comparator.comparingInt((player) -> player.getWorldLocation().distanceTo(EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation()))).get();
    }
}
