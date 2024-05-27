package com.lucidplugins.autodialog;

import com.google.inject.Provides;
import com.lucidplugins.api.utils.DialogUtils;
import com.lucidplugins.api.utils.MessageUtils;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@PluginDescriptor(
        name = "<html><font color=\"#32CD32\">Lucid </font>Auto Dialog</html>",
        description = "Does various dialog options automatically.",
        enabledByDefault = false,
        tags = {"dialog", "quest", "continue"}
)
@Singleton
public class LucidAutoDialogPlugin extends Plugin
{

    @Inject
    private LucidAutoDialogConfig config;

    @Inject
    private Client client;

    @Provides
    LucidAutoDialogConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucidAutoDialogConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick gameTick)
    {
        boolean didSomething = false;

        if (config.autoQuestDialog())
        {
           didSomething = doQuestDialog();
        }

        if (config.autoContinue() && !didSomething)
        {
            didSomething = doContinue();
        }
    }

    private boolean doQuestDialog()
    {
        List<String> options = DialogUtils.getOptions();
        if (options.isEmpty())
        {
            return false;
        }

        for (String option : DialogUtils.getOptions())
        {
            if (option.startsWith("["))
            {
                DialogUtils.selectOptionIndex(DialogUtils.getOptionIndex(option));
                return true;
            }
        }

        return false;
    }

    private boolean doContinue()
    {
        if (!DialogUtils.canContinue())
        {
            return false;
        }

        DialogUtils.sendContinueDialog();
        return true;
    }
}
