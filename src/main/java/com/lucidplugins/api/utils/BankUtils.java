package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Bank;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
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

    public static void depositAll() {
        Widget depositInventory = EthanApiPlugin.getClient().getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
        if (depositInventory != null && !depositInventory.isSelfHidden()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
        }
    }

    public static boolean withdraw1(int id)
    {
        if (Bank.search().withId(id).first().isEmpty())
        {
            return false;
        }

        BankInteraction.useItem(id, "Withdraw-1");
        return true;
    }

    public static boolean withdrawAll(int id)
    {
        if (Bank.search().withId(id).first().isEmpty())
        {
            return false;
        }

        BankInteraction.useItem(id, "Withdraw-All");
        return true;
    }

    public static void close()
    {
        if (isOpen())
        {
            EthanApiPlugin.getClient().runScript(29);
        }
    }
}
