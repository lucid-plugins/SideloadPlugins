package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Players;
import com.example.InteractionApi.PlayerInteractionHelper;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.RuneLite;

import java.util.List;
import java.util.function.Predicate;

public class PlayerUtils
{
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static void interactPlayer(String name, String action)
    {
        PlayerInteractionHelper.interact(name, action);
    }

    public static List<Player> getAll(Predicate<Player> filter)
    {
        return Players.search().filter(filter).result();
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
                float testDistance = InteractionUtils.distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation());

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
                float testDistance = InteractionUtils.distanceTo2DHypotenuse(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation());

                if (testDistance < closest)
                {
                    closestPlayer = p;
                    closest = testDistance;
                }
            }
        }

        return closestPlayer;
    }
}
