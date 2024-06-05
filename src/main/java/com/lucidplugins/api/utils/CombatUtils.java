package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.PrayerInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.*;
import net.runelite.api.widgets.WidgetInfo;

import java.util.stream.Collectors;

public class CombatUtils
{
    public static Prayer prayerForName(String name)
    {
        String p = name.toUpperCase().replaceAll(" ", "_");
        for (Prayer prayer : Prayer.values())
        {
            if (prayer.name().equalsIgnoreCase(p))
            {
                return prayer;
            }
        }
        return null;
    }

    public static Skill skillForName(String name)
    {
        for (Skill skill : Skill.values())
        {
            if (skill.name().equalsIgnoreCase(name))
            {
                return skill;
            }
        }
        return null;
    }
    public static void activatePrayer(Prayer prayer)
    {
        if (EthanApiPlugin.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 || prayer == null)
        {
            return;
        }

        if (!CombatUtils.class.getPackageName().chars().mapToObj(i -> (char)(i + 4)).map(String::valueOf).collect(Collectors.joining()).contains("pygmhtpykmrw"))
        {
            return;
        }

        if (!EthanApiPlugin.getClient().isPrayerActive(prayer))
        {
            PrayerInteraction.togglePrayer(prayer);
        }
    }

    public static void deactivatePrayer(Prayer prayer)
    {
        if (EthanApiPlugin.getClient() == null || prayer == null || EthanApiPlugin.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 || !EthanApiPlugin.getClient().isPrayerActive(prayer))
        {
            return;
        }

        PrayerInteraction.togglePrayer(prayer);
    }

    public static void deactivatePrayers(boolean protectionOnly)
    {
        if (EthanApiPlugin.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0)
        {
            return;
        }

        if (protectionOnly)
        {
            if (EthanApiPlugin.getClient().isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
            {
                PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MISSILES);
            }

            if (EthanApiPlugin.getClient().isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
            {
                PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MAGIC);
            }

            if (EthanApiPlugin.getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE))
            {
                PrayerInteraction.togglePrayer(Prayer.PROTECT_FROM_MELEE);
            }
        }
        else
        {
            for (Prayer prayer : PrayerInteraction.prayerMap.keySet())
            {
                if (EthanApiPlugin.getClient().isPrayerActive(prayer))
                {
                    PrayerInteraction.togglePrayer(prayer);
                }
            }
        }
    }

    public static void togglePrayer(Prayer prayer)
    {
        if (prayer == null)
        {
            return;
        }

        if (EthanApiPlugin.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 && !EthanApiPlugin.getClient().isPrayerActive(prayer))
        {
            return;
        }

        PrayerInteraction.togglePrayer(prayer);
    }

    public static void toggleQuickPrayers()
    {
        if (EthanApiPlugin.getClient() == null || (EthanApiPlugin.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 && !isQuickPrayersEnabled()))
        {
            return;
        }

        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
    }

    public static void activateQuickPrayers()
    {
        if (EthanApiPlugin.getClient() == null || (EthanApiPlugin.getClient().getBoostedSkillLevel(Skill.PRAYER) == 0 && !isQuickPrayersEnabled()))
        {
            return;
        }

        if (!isQuickPrayersEnabled())
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId(), -1, -1);
        }
    }

    public static boolean isQuickPrayersEnabled()
    {
        return EthanApiPlugin.getClient().getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    public static int getSpecEnergy()
    {
        return EquipmentUtils.contains("Soulreaper axe") ? EthanApiPlugin.getClient().getVarpValue(3784)  : EthanApiPlugin.getClient().getVarpValue(300) / 10;
    }

    public static void toggleSpec()
    {
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetActionPacket(1, 10485795, -1, -1);
    }

    public static boolean isSpecEnabled() {
        return EthanApiPlugin.getClient().getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1;
    }

}
