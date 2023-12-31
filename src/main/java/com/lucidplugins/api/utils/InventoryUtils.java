package com.lucidplugins.api.utils;


import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.InteractionApi.InventoryInteraction;
import com.lucidplugins.api.item.SlottedItem;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InventoryUtils
{
    public static List<SlottedItem> getAll()
    {
        return Inventory.search().result().stream().map(item -> new SlottedItem(item.getId(), item.getItemQuantity(), item.getIndex())).collect(Collectors.toList());
    }

    public static List<SlottedItem> getAll(Predicate<SlottedItem> filter)
    {
        return Inventory.search().result().stream().map(item -> new SlottedItem(item.getId(), item.getItemQuantity(), item.getIndex())).filter(filter).collect(Collectors.toList());
    }

    public static boolean contains(String itemName)
    {
        return Inventory.search().nameContains(itemName).first().isPresent();
    }

    public static boolean contains(int[] ids)
    {
        List<Integer> intIdList = Arrays.stream(ids).boxed().collect(Collectors.toList());
        return !Inventory.search().idInList(intIdList).result().isEmpty();
    }

    public static int getFreeSlots()
    {
        return Inventory.getEmptySlots();
    }

    public static boolean itemHasAction(Client client, int itemId, String action)
    {
        return Arrays.stream(client.getItemDefinition(itemId).getInventoryActions()).anyMatch(a -> a != null && a.equalsIgnoreCase(action));
    }

    public static void itemInteract(int itemId, String action)
    {
        InventoryInteraction.useItem(itemId, action);
    }

    public static int calculateWidgetId(Client client, Item item)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget == null)
        {
            return -1;
        }
        else
        {
            Widget[] children = inventoryWidget.getChildren();
            return children == null ? -1 : (Integer) Arrays.stream(children).filter((x) -> {
                return x.getItemId() == item.getId();
            }).findFirst().map(Widget::getId).orElse(-1);
        }
    }

    public static SlottedItem getFirstItem(int id)
    {
        Widget itemWidget = Inventory.search().withId(id).first().orElse(null);
        int amount = -1;

        if (itemWidget != null)
        {
            amount = itemWidget.getItemQuantity();
        }

        if (id == -1 || amount == -1)
        {
            return null;
        }

        return new SlottedItem(id, amount, itemWidget.getIndex());
    }

    public static SlottedItem getFirstItem(int[] ids)
    {
        List<Integer> intIdList = Arrays.stream(ids).boxed().collect(Collectors.toList());

        Widget itemWidget = Inventory.search().idInList(intIdList).first().orElse(null);
        int amount = -1;

        if (itemWidget != null)
        {
            amount = itemWidget.getItemQuantity();
        }

        if (itemWidget.getItemId() == -1 || amount == -1)
        {
            return null;
        }

        return new SlottedItem(itemWidget.getItemId(), amount, itemWidget.getIndex());
    }

    public static int getItemId(String name)
    {
        final Widget itemWidget = Inventory.search().filter(item -> item.getName().contains(name)).first().orElse(null);
        return itemWidget != null ? itemWidget.getItemId() : -1;
    }

    public static Item getFirstItem(String name)
    {
        Widget itemWidget = Inventory.search().nameContains(name).first().orElse(null);
        Item item = null;

        if (itemWidget != null)
        {
            item = new Item(itemWidget.getItemId(), itemWidget.getItemQuantity());
        }
        return item;
    }

    public static void wieldItem(int id)
    {

    }
}
