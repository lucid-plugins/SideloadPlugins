package com.lucidplugins.autodialog;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("lucid-auto-dialog")
public interface LucidAutoDialogConfig extends Config
{
    // General section
    @ConfigSection(
            name = "General Settings",
            description = "Change the main settings",
            position = 0,
            closedByDefault = true
    )
    String generalSection = "General Settings";

    @ConfigItem(name = "Auto-quest helper dialog",
            description = "If there is a dialog quest helper highlights, it will select that option automatically",
            position = 0,
            keyName = "autoQuestDialog",
            section = generalSection
    )
    default boolean autoQuestDialog()
    {
        return false;
    }

    @ConfigItem(name = "Auto-continue",
            description = "If there is a 'Press space to continue' option, it will be selected automatically.",
            position = 1,
            keyName = "autoContinue",
            section = generalSection
    )
    default boolean autoContinue()
    {
        return false;
    }
}
