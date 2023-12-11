package com.lucidplugins.lucidgearswapper;

import lombok.Getter;
import net.runelite.client.config.Keybind;

public class ExportableConfig
{
    @Getter
    public boolean[] swapEnabled;

    @Getter
    public String[] swapString;

    @Getter
    public Keybind[] swapHotkey;

    @Getter
    public boolean[] equipFirstItem;

    public ExportableConfig()
    {
        swapEnabled = new boolean[6];
        swapString = new String[6];
        swapHotkey = new Keybind[6];
        equipFirstItem = new boolean[6];
    }

    public void setSwap(int index, final boolean swapEnabled, final String swapString,
                        final Keybind swapHotkey, final boolean equipFirstItem)
    {
        this.swapEnabled[index] = swapEnabled;
        this.swapString[index] = swapString;
        this.swapHotkey[index] = swapHotkey;
        this.equipFirstItem[index] = equipFirstItem;
    }

}
