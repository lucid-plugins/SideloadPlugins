package com.lucidplugins.oneclickagility;

import com.example.EthanApiPlugin.Collections.NPCs;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import java.util.Collection;
import java.util.Collections;


public class WerewolfObstacleArea extends ObstacleArea
{
    WerewolfObstacleArea()
    {
        super(3523, 3549, 9861, 9897, 0, -1);
    }

    WorldPoint point = new WorldPoint(3538, 9874, 0);

    @Override
    public MenuEntry createMenuEntry(Client client)
    {
        NPC werewolf = NPCs.search().withId(5927).first().orElse(null);
        if (client.getLocalPlayer().getWorldLocation().getY() > 9875)
        {
            return client.createMenuEntry(-1)
                    .setOption("Walk here")
                    .setTarget("")
                    .setIdentifier(0)
                    .setType(MenuAction.WALK)
                    .setParam0(3528)
                    .setParam1(9866)
                    .setWorldViewId(client.getTopLevelWorldView().getId())
                    .setForceLeftClick(false);
        }
        else if (getItems(Collections.singletonList(ItemID.STICK),client) != null && werewolf != null)
            return client.createMenuEntry(-1)
                    .setOption("Give-Stick")
                    .setTarget("Agility Trainer")
                    .setIdentifier(werewolf.getIndex())
                    .setType(MenuAction.NPC_FIRST_OPTION)
                    .setParam0(0)
                    .setParam1(0)
                    .setWorldViewId(client.getTopLevelWorldView().getId())
                    .setForceLeftClick(false);
        else
            return client.createMenuEntry(-1)
                    .setOption("Walk here")
                    .setTarget("")
                    .setIdentifier(0)
                    .setType(MenuAction.WALK)
                    .setParam0(point.getX())
                    .setParam1(point.getY())
                    .setWorldViewId(client.getTopLevelWorldView().getId())
                    .setForceLeftClick(false);

    }


    public Widget getItems(Collection<Integer> ids, Client client)
    {
        client.runScript(6009, 9764864, 28, 1, -1);
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null && inventoryWidget.getDynamicChildren() != null)
        {
            Widget[] items = inventoryWidget.getDynamicChildren();
            for(Widget item : items)
            {
                if (ids.contains(item.getItemId()))
                {
                    return item;
                }
            }
        }
        return null;
    }

}
