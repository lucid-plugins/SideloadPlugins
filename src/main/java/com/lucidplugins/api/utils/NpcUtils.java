package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.InteractionApi.NPCInteraction;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.RuneLite;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NpcUtils
{
    static Client client = RuneLite.getInjector().getInstance(Client.class);

    public static List<NPC> getAll(Predicate<NPC> filter)
    {
        return NPCs.search().filter(filter).result();
    }

    public static NPC getNearestNpc(Predicate<NPC> filter)
    {
        return NPCs.search().filter(filter).nearestToPlayer().orElse(null);
    }

    public static NPC getNearestNpc(String name)
    {
        return NPCs.search().nameContains(name).nearestToPlayer().orElse(null);
    }

    public static NPC getNearestNpc(int id)
    {
        return NPCs.search().withId(id).nearestToPlayer().orElse(null);
    }

    public static List<NPC> getAllNpcs(int... ids)
    {
        return NPCs.search().idInList(Arrays.stream(ids).boxed().collect(Collectors.toList())).result();
    }

    public static void attackNpc(NPC npc)
    {
        if (npc == null)
        {
            return;
        }

        NPCInteraction.interact(npc, "Attack");
    }

    public static void interact(NPC npc, String action)
    {
        if (npc == null)
        {
            return;
        }

        NPCInteraction.interact(npc, action);
    }
}
