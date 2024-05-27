package com.lucidplugins.api.utils;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DialogUtils
{
    private static List<Integer> continueParentIds = List.of(193, 229, 229, 231, 217);
    private static List<Integer> continueChildIds = List.of(   0,   0,   2,   5,   5);

    public static void queueResumePauseDialog(int widgetId, int childId)
    {
        WidgetPackets.queueResumePause(widgetId, childId);
    }

    public static List<String> getOptions()
    {
        Widget widget = EthanApiPlugin.getClient().getWidget(219, 1);
        if (widget == null || widget.isSelfHidden())
        {
            return Collections.emptyList();
        }
        else
        {
            List<String> out = new ArrayList<>();
            Widget[] children = widget.getChildren();
            if (children == null)
            {
                return out;
            }
            else
            {
                for (int i = 1; i < children.length; ++i)
                {
                    if (children[i] != null && !children[i].getText().isBlank())
                    {
                        out.add(children[i].getText());
                    }
                }

                return out;
            }
        }
    }

    public static boolean canContinue()
    {
        for (int i = 0; i < continueParentIds.size(); i++)
        {
            if (!InteractionUtils.isWidgetHidden(continueParentIds.get(i), continueChildIds.get(i)))
            {
                return true;
            }
        }

        return false;
    }

    public static void sendContinueDialog()
    {
        for (int i = 0; i < continueParentIds.size(); i++)
        {
            if (!InteractionUtils.isWidgetHidden(continueParentIds.get(i), continueChildIds.get(i)))
            {
                queueResumePauseDialog(continueParentIds.get(i) << 16 | continueChildIds.get(i), -1);
            }
        }
    }

    public static void selectOptionIndex(int index)
    {
        Widget widget = EthanApiPlugin.getClient().getWidget(219, 1);
        if (widget == null || widget.isSelfHidden())
        {
            return;
        }

        queueResumePauseDialog(219 << 16 | 1, index);

    }

    public static int getOptionIndex(String option)
    {
        if (getOptions().isEmpty())
        {
            return -1;
        }

        List<String> options = getOptions();
        for (int index = 0; index < options.size(); index++)
        {
            if (options.get(index).contains(option))
            {
                return index + 1;
            }
        }

        return -1;
    }
}
