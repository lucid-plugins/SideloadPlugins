package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.InteractionApi.PrayerInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;

public class CombatUtils
{
    public static Prayer prayerForName(String name)
    {
        String p = name.toUpperCase().replaceAll(" ", "_");
        for (Prayer prayer : Prayer.values())
        {
            if (prayer.name().equals(p))
            {
                return prayer;
            }
        }
        return null;
    }

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

    public static void toggleQuickPrayers(Client client)
    {
        if (client == null || (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && !isQuickPrayersEnabled(client)))
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
    }

    public static void activateQuickPrayers(Client client)
    {
        if (client == null || (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && !isQuickPrayersEnabled(client)))
        {
            return;
        }

        if (!isQuickPrayersEnabled(client))
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
        }
    }

    public static boolean isQuickPrayersEnabled(Client client)
    {
        return client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
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

       toggleSpec();
    }

    public static void toggleSpec()
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, 38862884, -1, -1);
    }

}
