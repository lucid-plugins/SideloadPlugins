package com.lucidplugins.lucidpvpphelper;

import com.lucidplugins.api.Weapon;
import com.lucidplugins.api.utils.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Opponent
{
    private List<Integer> switchTicks = new ArrayList<>();

    @Getter
    @Setter
    private int mostRecentInteractionTick = 0;

    @Getter
    @Setter
    private int lastAmimationTick = 0;

    @Getter
    private Weapon currentWeapon;

    public boolean isSpamSwitching(int currentTick)
    {
        int switches = 0;
        for (int tick : switchTicks)
        {
            if (currentTick - tick <= 5)
            {
                switches++;
            }
        }

        return switches >= 3;
    }

    public void setWeapon(Weapon weapon, int tick)
    {
        if (weapon == null || currentWeapon != weapon)
        {
            MessageUtils.addMessage("Setting opponents weapon from " + currentWeapon + " to: " + weapon + ", tick: " + tick, Color.RED);
            currentWeapon = weapon;
            switchTicks.add(tick);
        }
    }

    public void resetSwitchTicks()
    {
        switchTicks.clear();
    }
}
