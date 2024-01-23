package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.EthanApiPlugin;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

public class BankUtils
{
    public static boolean isOpen()
    {
        Widget bankWidget = EthanApiPlugin.getClient().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankWidget != null && !bankWidget.isSelfHidden())
        {
            return true;
        }

        return false;
    }

    public static void close()
    {
        EthanApiPlugin.getClient().runScript(29);
    }
}
