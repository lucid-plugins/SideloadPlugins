package com.lucidplugins.api.utils;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DialogUtils
{
    public static boolean hasOption(Client client, String option)
    {
        return hasOption(client, s -> s.equalsIgnoreCase(option));
    }

    public static boolean hasOption(Client client, Predicate<String> option)
    {
        return getOptions(client).stream().map(Widget::getText).filter(Objects::nonNull).anyMatch(option);
    }

    public static List<Widget> getOptions(Client client)
    {
        Widget widget = client.getWidget(219, 1);
        if (widget == null || widget.isSelfHidden())
        {
            return Collections.emptyList();
        }
        else
        {
            List<Widget> out = new ArrayList();
            Widget[] children = widget.getChildren();
            if (children == null)
            {
                return out;
            }
            else
            {
                for (int i = 1; i < children.length; ++i)
                {
                    if (!children[i].getText().isBlank())
                    {
                        out.add(children[i]);
                    }
                }

                return out;
            }
        }
    }
}
