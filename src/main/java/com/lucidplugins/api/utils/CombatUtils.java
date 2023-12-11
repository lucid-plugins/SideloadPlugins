package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.InteractionApi.PrayerInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;

public class CombatUtils
{
    public static void activatePrayer(Client client, Prayer prayer)
    {
        if (client.getBoostedSkillLevel(Skill.PRAYER) == 0)
        {
            return;
        }

        if (!client.isPrayerActive(prayer))
        {
            PrayerInteraction.togglePrayer(prayer);
        }
    }

    public static void deactivatePrayers(Client client, boolean protectionOnly)
    {
        if (protectionOnly)
        {
            if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
            {
                PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MISSILES);
            }

            if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
            {
                PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MAGIC);
            }

            if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE))
            {
                PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MELEE);
            }
        }
        else
        {
            for (Prayer prayer : PrayerInteraction.prayerMap.keySet())
            {
                if (client.isPrayerActive(prayer))
                {
                    PrayerInteraction.togglePrayer(prayer);
                }
            }
        }
    }

    public static void togglePrayer(Client client, Prayer prayer)
    {
        if (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && !client.isPrayerActive(prayer))
        {
            return;
        }

        PrayerInteraction.togglePrayer(prayer);
    }

    public static int getSpecEnergy(Client client)
    {
        return client.getVarpValue(300) / 10;
    }

    public static void quickKerisSpec(Client client)
    {
        if (client == null)
        {
            return;
        }

        Item keris = InventoryUtils.getFirstItem("Keris partisan of the sun");
        boolean kerisEquipped = Equipment.search().nameContains("of the sun").first().isPresent();
        if ((keris == null && !kerisEquipped) || CombatUtils.getSpecEnergy(client) < 75)
        {
            return;
        }

        if (!kerisEquipped)
        {
            InventoryUtils.itemInteract(keris.getId(), "Wield");
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, 38862884, -1, -1);
    }
}
