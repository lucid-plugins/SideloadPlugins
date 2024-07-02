package com.lucidplugins.lucidwildytele;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("lucid-wildy-tele")
public interface LucidWildyTeleConfig extends Config
{
    @ConfigItem(
            name = "Always tele if within combat bracket",
            description = "Teleports regardless if the player is skulled or not as long as theyre able to attack you.",
            position = 0,
            keyName = "alwaysTele"
    )
    default boolean alwaysTele()
    {
        return false;
    }

    @ConfigItem(
            name = "Ignore Wilderness level",
            description = "Doesn't check wildy level, so it will tele if there is a player around, regardless of level.",
            position = 0,
            keyName = "ignoreWildyLevel"
    )
    default boolean ignoreWildyLevel()
    {
        return false;
    }
}
