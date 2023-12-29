package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.Collections.Equipment;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.lucidplugins.api.item.SlottedItem;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.List;
import java.util.stream.Collectors;

public class EquipmentUtils
{

    public static List<SlottedItem> getAll()
    {
        return Equipment.search().result().stream().map(equipmentItemWidget -> new SlottedItem(equipmentItemWidget.getEquipmentItemId(), equipmentItemWidget.getItemQuantity(), equipmentItemWidget.getEquipmentIndex())).collect(Collectors.toList());
    }

    public static List<SlottedItem> getAll(Predicate<SlottedItem> filter)
    {
        return Equipment.search().result().stream().map(equipmentItemWidget -> new SlottedItem(equipmentItemWidget.getEquipmentItemId(), equipmentItemWidget.getItemQuantity(), equipmentItemWidget.getEquipmentIndex())).filter(filter).collect(Collectors.toList());
    }

    public static int getWepSlotItemId()
    {
        Widget itemWidget = Equipment.search().indexIs(3).first().orElse(null);
        int id = -1;
        int amount = -1;

        if (itemWidget != null)
        {
            id = itemWidget.getItemId();
        }

        return id;
    }

    public static Item getWepSlotItem()
    {
        Widget itemWidget = Equipment.search().indexIs(3).first().orElse(null);
        int id = -1;
        int amount = -1;

        if (itemWidget != null)
        {
            id = itemWidget.getItemId();
            amount = itemWidget.getItemQuantity();
        }

        if (id == -1 || amount == -1)
        {
            return null;
        }
        return new Item(id, amount);
    }

    public static boolean contains(int[] ids)
    {
        List<Integer> intIdList = Arrays.stream(ids).boxed().collect(Collectors.toList());

        return !Equipment.search().idInList(intIdList).result().isEmpty();
    }

    public static void removeWepSlotItem()
    {
        Widget itemWidget = Equipment.search().indexIs(3).first().orElse(null);

        if (itemWidget != null)
        {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(itemWidget, "Remove");
        }
    }
}
