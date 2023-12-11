package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;

public class InteractionUtils
{

    public static boolean sleep(Client client, long ms)
    {
        if (client.isClientThread())
        {
            return false;
        }
        else
        {
            try
            {
                Thread.sleep(ms);
                return true;
            }
            catch (InterruptedException var3)
            {
                return false;
            }
        }
    }

    public static void walk(WorldPoint point)
    {
        MousePackets.queueClickPacket();
        MovementPackets.queueMovement(point);
    }
}
