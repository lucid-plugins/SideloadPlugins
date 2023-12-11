package com.lucidplugins.api.utils;

import net.runelite.api.Client;

import java.awt.*;
import java.awt.event.KeyEvent;

public class KeyboardUtils
{
    public static void typeKey(Client client, char c)
    {
        Canvas canvas = client.getCanvas();
        long time = System.currentTimeMillis();
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        KeyEvent pressed = new KeyEvent(canvas, 401, time, 0, keyCode, c, 1);
        KeyEvent typed = new KeyEvent(canvas, 400, time, 0, 0, c, 0);
        canvas.dispatchEvent(pressed);
        canvas.dispatchEvent(typed);
        InteractionUtils.sleep(client, 10L);
        KeyEvent released = new KeyEvent(canvas, 402, System.currentTimeMillis(), 0, keyCode, c, 1);
        canvas.dispatchEvent(released);
    }

    public static void type(Client client, int number)
    {
        type(client, String.valueOf(number));
    }

    public static void type(Client client, String text)
    {
        type(client, text, false);
    }

    public static void type(Client client, String text, boolean sendEnter)
    {
        char[] chars = text.toCharArray();
        char[] var3 = chars;
        int var4 = chars.length;

        for (int var5 = 0; var5 < var4; ++var5)
        {
            char c = var3[var5];
            type(client, c);
        }

        if (sendEnter)
        {
            sendEnter(client);
        }
    }

    public static void sendSpace(Client client)
    {
        typeKey(client, ' ');
    }

    public static void sendEnter(Client client)
    {
        typeKey(client, '\n');
    }
}
