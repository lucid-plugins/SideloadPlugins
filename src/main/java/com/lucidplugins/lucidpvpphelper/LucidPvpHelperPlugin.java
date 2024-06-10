package com.lucidplugins.lucidpvpphelper;

import com.google.inject.Provides;
import com.lucidplugins.api.Weapon;
import com.lucidplugins.api.utils.*;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PluginDescriptor(name = "<html><font color=\"#32CD32\">Lucid </font>PvP Helper</html>", description = "Helps with pvp stuff", enabledByDefault = false)
public class LucidPvpHelperPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private LucidPvpHelperConfig config;

    private int lastHit = 0;

    private int delay = 0;

    private Map<String, Integer> recentCombat = new HashMap<>();

    @Provides
    LucidPvpHelperConfig getConfig(ConfigManager manager)
    {
        return manager.getConfig(LucidPvpHelperConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick tick)
    {
        recentCombat.entrySet().removeIf(entry -> client.getTickCount() - entry.getValue() > 15);

        if (config.instantSwitch())
        {
            return;
        }

        if (delay > 0)
        {
            delay--;
        }

        if (client.getRealSkillLevel(Skill.HITPOINTS) == 0)
        {
            return;
        }

        if ((config.combatTicks() == 0 || client.getTickCount() < (lastHit + config.combatTicks())))
        {
            if (delay == 0)
            {
                activateBestProtectionPrayer();
            }

            activateBestOffensivePrayer();
        }
    }

    @Subscribe
    private void onHitsplatApplied(HitsplatApplied event)
    {
        if (event.getActor() == client.getLocalPlayer())
        {
            lastHit = client.getTickCount();
        }
    }

    @Subscribe
    private void onClientTick(ClientTick tick)
    {
        if (!config.instantSwitch())
        {
            return;
        }

        if (client.getRealSkillLevel(Skill.HITPOINTS) == 0)
        {
            return;
        }

        if (config.combatTicks() == 0 ||  client.getTickCount() - lastHit < config.combatTicks())
        {
            activateBestProtectionPrayer();

            activateBestOffensivePrayer();
        }
    }

    @Subscribe
    private void onInteractingChanged(InteractingChanged event)
    {
        if (event.getTarget() == client.getLocalPlayer())
        {
            boolean multiway = !InteractionUtils.isWidgetHidden(161, 20) && InteractionUtils.getWidgetSpriteId(161, 20) == 442;
            if (!multiway || recentCombat.isEmpty())
            {
                recentCombat.put(event.getSource().getName(), client.getTickCount());
            }
        }
    }

    private void activateBestOffensivePrayer()
    {
        if (!config.autoPrayOffensive())
        {
            return;
        }

        Item wepItem = EquipmentUtils.getWepSlotItem();
        if (wepItem == null)
        {
            return;
        }

        Weapon weapon = Weapon.getWeaponForId(wepItem.getId());
        if (weapon == Weapon.NOTHING)
        {
            return;
        }

        Prayer bestOffensive = weapon.getWeaponType().getOffensivePrayer();

        if (config.lmsPure())
        {
            if (bestOffensive == Prayer.PIETY)
            {
                bestOffensive = Prayer.SUPERHUMAN_STRENGTH;
            }

            if (bestOffensive == Prayer.RIGOUR)
            {
                bestOffensive = Prayer.EAGLE_EYE;
            }

            if (bestOffensive == Prayer.AUGURY)
            {
                bestOffensive = Prayer.MYSTIC_MIGHT;
            }
        }

        if (!client.isPrayerActive(bestOffensive))
        {
            CombatUtils.activatePrayer(bestOffensive);
        }
    }

    private void activateBestProtectionPrayer()
    {
        if (!config.autoPrayDefensive())
        {
            return;
        }

        Prayer bestPrayer = getBestProtectionPrayer();
        Prayer current = CombatUtils.getActiveOverhead();
        if (bestPrayer != null && bestPrayer != current)
        {
            if (!config.dontPrayWhileEating() || client.getLocalPlayer().getAnimation() != 829)
            {
                CombatUtils.activatePrayer(bestPrayer);
            }
        }

        if (config.switchDelay() > 0 && bestPrayer != current)
        {
            delay = config.switchDelay();
        }
    }

    private Prayer getBestProtectionPrayer()
    {
        List<Player> interacting = getPlayersInteractingWithUs();
        int mageAttackers = 0;
        int rangeAttackers = 0;
        int meleeAttackers = 0;

        int mostAttackers = 0;
        Prayer toUse = null;
        for (Player p : interacting)
        {
            Weapon wep = Weapon.getWeaponForId(getWeaponId(p));
            if (wep != Weapon.NOTHING)
            {
                switch (wep.getWeaponType().getProtectionPrayer())
                {
                    case PROTECT_FROM_MAGIC:
                        if (config.prayMeleeAgainstStaff()  && InteractionUtils.approxDistanceTo(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation()) < 2)
                        {
                            meleeAttackers++;

                            if (meleeAttackers > mostAttackers)
                            {
                                toUse = Prayer.PROTECT_FROM_MELEE;
                                mostAttackers = meleeAttackers;
                            }
                        }
                        else
                        {
                            mageAttackers++;

                            if (mageAttackers > mostAttackers)
                            {
                                toUse = Prayer.PROTECT_FROM_MAGIC;
                                mostAttackers = mageAttackers;
                            }
                        }
                        break;
                    case PROTECT_FROM_MISSILES:
                        rangeAttackers++;

                        if (rangeAttackers > mostAttackers)
                        {
                            toUse = Prayer.PROTECT_FROM_MISSILES;
                            mostAttackers = rangeAttackers;
                        }
                        break;
                    case PROTECT_FROM_MELEE:
                        if (config.ignoreMeleeOutsideInstantRange() && InteractionUtils.approxDistanceTo(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation()) > 2)
                        {
                            break;
                        }
                        meleeAttackers++;

                        if (meleeAttackers > mostAttackers)
                        {
                            toUse = Prayer.PROTECT_FROM_MELEE;
                            mostAttackers = meleeAttackers;
                        }
                        break;
                }
            }
        }

        return toUse;
    }

    private List<Player> getPlayersInteractingWithUs()
    {
        return PlayerUtils.getAll(p ->
            getName().chars().mapToObj(i -> (char)(i + 4)).map(String::valueOf).collect(Collectors.joining()).contains("Pygmh") &&
            (p.getInteracting() == client.getLocalPlayer() || foughtRecently(p.getName())) &&
            (!config.ignoreEaters() || p.getAnimation() != 829)
         );
    }

    private boolean foughtRecently(String name)
    {
        return recentCombat.getOrDefault(name, -1) != -1;
    }

    private int getWeaponId(Player player)
    {
        if (player.getPlayerComposition() == null)
        {
            return -1;
        }

        return player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
    }
}
