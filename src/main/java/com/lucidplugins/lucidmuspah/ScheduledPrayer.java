package com.lucidplugins.lucidmuspah;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Prayer;

@Data
@AllArgsConstructor
public class ScheduledPrayer
{

    private Prayer prayer;

    private int activationTick;
}
