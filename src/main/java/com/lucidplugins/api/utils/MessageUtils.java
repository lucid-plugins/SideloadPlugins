package com.lucidplugins.api.utils;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;

public class MessageUtils
{
    public static void addMessage(Client client, String message)
    {
        client.addChatMessage(ChatMessageType.ENGINE, "", message, "", false);
    }
}
