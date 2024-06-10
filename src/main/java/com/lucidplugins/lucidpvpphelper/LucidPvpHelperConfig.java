package com.lucidplugins.lucidpvpphelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lucid-pvp-helper")
public interface LucidPvpHelperConfig extends Config
{
    @ConfigItem(
            name = "Auto-pray defensive",
            description = "Auto-prays against best defensive prayer",
            position = 0,
            keyName = "autoPrayDefensive"
    )
    default boolean autoPrayDefensive()
    {
        return false;
    }

    @ConfigItem(
            name = "Auto-pray offensive",
            description = "Auto-prays the best offensive prayer based on your weapon",
            position = 1,
            keyName = "autoPrayOffensive"
    )
    default boolean autoPrayOffensive()
    {
        return false;
    }

    @ConfigItem(
            name = "Instant switch",
            description = "Does prayer detections more often than 1 time per tick",
            position = 1,
            keyName = "instantSwitch"
    )
    default boolean instantSwitch()
    {
        return false;
    }

    @ConfigItem(
            name = "Switch Tick Delay",
            description = "How many ticks to delay switching prayers",
            position = 2,
            keyName = "switchDelay"
    )
    default int switchDelay()
    {
        return 0;
    }

    @ConfigItem(
            name = "Out Of Combat Ticks",
            description = "Will only pray if its been less than this amount of ticks since you received a hitsplat. -1 for always on.",
            position = 3,
            keyName = "combatTicks"
    )
    default int combatTicks()
    {
        return 0;
    }

    @ConfigItem(
            name = "Ignore Players Eating",
            description = "Will ignore players doing the eat animation to hide their weapon swaps",
            position = 4,
            keyName = "ignoreEaters"
    )
    default boolean ignoreEaters()
    {
        return false;
    }

    @ConfigItem(
            name = "Don't Pray While Eating",
            description = "Won't swap prayers while you're doing the eating animation",
            position = 5,
            keyName = "dontPrayWhileEating"
    )
    default boolean dontPrayWhileEating()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore Melee Outside Range",
            description = "Will ignore prayers for players with melee weapons and are more than 2 tiles away",
            position = 6,
            keyName = "ignoreMeleeOutsideInstantRange"
    )
    default boolean ignoreMeleeOutsideInstantRange()
    {
        return false;
    }

    @ConfigItem(
            name = "Pray Melee Staff",
            description = "Prays melee against staves when in melee range",
            position = 7,
            keyName = "prayMeleeAgainstStaff"
    )
    default boolean prayMeleeAgainstStaff()
    {
        return false;
    }

    @ConfigItem(
            name = "LMS Pure",
            description = "Doesn't use rigour/piety/augury even if unlocked",
            position = 8,
            keyName = "lmsPure"
    )
    default boolean lmsPure()
    {
        return false;
    }
}
