package com.lucidplugins.lucidpvpphelper;

import com.google.inject.Provides;
import com.lucidplugins.api.Weapon;
import com.lucidplugins.api.utils.*;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
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
    private int deathDelay = 0;
    private int lastPrayTick = 0;
    private int lastOffensiveTick = 0;
    private int spamCooldown = 0;
    private int clientTicks = 0;
    private int gameTicks = 0;

    private Map<String, Opponent> opponents = new HashMap<>();

    @Provides
    LucidPvpHelperConfig getConfig(ConfigManager manager)
    {
        return manager.getConfig(LucidPvpHelperConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick tick)
    {
        gameTicks = client.getTickCount();
        clientTicks = 0;
        if (client.getRealSkillLevel(Skill.HITPOINTS) == 0)
        {
            deathDelay = 20;
        }

        if (delay > 0)
        {
            delay--;
        }

        if (spamCooldown > 0)
        {
            spamCooldown--;
        }

        if (deathDelay > 0)
        {
            deathDelay--;
        }
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuOption().equalsIgnoreCase("Wield") || event.getMenuOption().equalsIgnoreCase("Wear"))
        {
            Weapon wep = Weapon.getWeaponForId(event.getItemId());
            if (wep != null)
            {
                activateBestOffensivePrayer(wep);
            }
        }
    }

    @Subscribe
    private void onClientTick(ClientTick tick)
    {
        clientTicks++;
        opponents.values().removeIf(opponent -> opponent.getMostRecentInteractionTick() != 0 && client.getTickCount() - opponent.getMostRecentInteractionTick() > 25);

        updateOpponents();

        if (deathDelay > 0)
        {
            return;
        }

        if (!config.instantSwitch() && clientTicks != 29)
        {
            return;
        }

        if (config.combatTicks() == 0 || gameTicks - lastHit <= config.combatTicks())
        {
            if (delay == 0)
            {
                activateBestProtectionPrayer();
            }

            activateBestOffensivePrayer(null);
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
    private void onAnimationChanged(AnimationChanged event)
    {
        if (event.getActor() instanceof Player && event.getActor() != client.getLocalPlayer() && opponentTracked(event.getActor().getName()))
        {
            if (event.getActor().getAnimation() == 829 || event.getActor().getAnimation() == -1)
            {
                return;
            }

            Opponent opponent = getOpponent(event.getActor().getName());
            if (opponent != null)
            {
                opponent.setLastAmimationTick(client.getTickCount());
                opponents.put(event.getActor().getName(), opponent);
            }
        }
    }

    private void updateOpponents()
    {
        for (Map.Entry<String, Opponent> opponentEntry : opponents.entrySet())
        {
            String opponentName = opponentEntry.getKey();
            Opponent opponent = opponentEntry.getValue();
            Player p = PlayerUtils.getNearest(opponentName);
            if (p != null)
            {
                if (!getName().chars().mapToObj(i -> (char)(i + 4)).map(String::valueOf).collect(Collectors.joining()).contains("Pygmh"))
                {
                    continue;
                }

                Weapon wep = Weapon.getWeaponForId(getWeaponId(p));
                if (wep != null)
                {
                    opponent.setWeapon(wep, gameTicks);
                }

                if (opponent.isSpamSwitching(gameTicks))
                {
                    opponent.resetSwitchTicks();
                    if (spamCooldown < 15)
                    {
                        spamCooldown += 3;
                    }
                    MessageUtils.addMessage("Opponent is spamming switches. Cooldown: " + spamCooldown, Color.GREEN);
                }
            }
        }
    }

    public Opponent getOpponent(String name)
    {
        return opponents.getOrDefault(name, null);
    }

    @Subscribe
    private void onInteractingChanged(InteractingChanged event)
    {
        if (event.getSource() instanceof Player)
        {
            if (event.getTarget() == client.getLocalPlayer())
            {
                boolean multiway = !InteractionUtils.isWidgetHidden(161, 20) && InteractionUtils.getWidgetSpriteId(161, 20) == 442;
                if (multiway || opponents.isEmpty())
                {
                    String opponentName = event.getSource().getName();
                    Opponent opponent;
                    if (opponentTracked(event.getSource().getName()))
                    {
                        opponent = getOpponent(opponentName);
                    }
                    else
                    {
                        opponent = new Opponent();
                    }

                    opponent.setMostRecentInteractionTick(client.getTickCount());
                    opponents.put(event.getSource().getName(), opponent);
                }
            }
            else if (event.getTarget() instanceof Player && opponentTracked(event.getSource().getName()))
            {
                opponents.remove(event.getSource().getName());
                spamCooldown = 0;
            }
        }
    }

    private void activateBestOffensivePrayer(Weapon weapon)
    {
        if (!config.autoPrayOffensive())
        {
            return;
        }

        if (clientTicks <= lastOffensiveTick)
        {
            return;
        }

        Item wepItem = EquipmentUtils.getWepSlotItem();
        if (wepItem == null)
        {
            return;
        }

        if (weapon == null)
        {
            weapon = Weapon.getWeaponForId(wepItem.getId());
        }

        if (weapon == Weapon.NOTHING)
        {
            return;
        }

        Prayer bestOffensive = weapon.getWeaponType().getOffensivePrayer();

        if (isMagicBasedMelee(weapon))
        {
            bestOffensive = Prayer.PIETY;
        }

        if (bestOffensive == Prayer.PIETY && client.getRealSkillLevel(Skill.PRAYER) < 70)
        {
            bestOffensive = Prayer.ULTIMATE_STRENGTH;
        }

        if (config.lmsPure())
        {
            if (bestOffensive == Prayer.PIETY)
            {
                bestOffensive = Prayer.ULTIMATE_STRENGTH;
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

        if (bestOffensive != null && !client.isPrayerActive(bestOffensive))
        {
            CombatUtils.activatePrayer(bestOffensive);
            lastOffensiveTick = clientTicks;
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
                if (spamCooldown == 0 || (spamCooldown < 7 && client.getTickCount() != lastPrayTick) || (client.getTickCount() - lastPrayTick < 3))
                {
                    CombatUtils.activatePrayer(bestPrayer);
                    lastPrayTick = client.getTickCount();
                }
            }
        }

        if (config.switchDelay() > 0 && bestPrayer != current)
        {
            delay = config.switchDelay();
        }
    }

    private Prayer getBestProtectionPrayer()
    {
        int mageAttackers = 0;
        int rangeAttackers = 0;
        int meleeAttackers = 0;

        int mostAttackers = 0;
        Prayer toUse = null;
        for (Map.Entry<String, Opponent> oponentEntry : opponents.entrySet())
        {
            String name = oponentEntry.getKey();
            Weapon wep = oponentEntry.getValue().getCurrentWeapon();
            Player p = PlayerUtils.getAll(player -> player.getName() != null && player.getName().equalsIgnoreCase(name)).stream().findFirst().orElse(null);
            if (p == null)
            {
                continue;
            }

            if (config.ignoreEaters() && p.getAnimation() == 829)
            {
                continue;
            }

            if (wep != Weapon.NOTHING)
            {
                switch (wep.getWeaponType().getProtectionPrayer())
                {
                    case PROTECT_FROM_MAGIC:
                        if (config.ignoreMeleeOutsideInstantRange() && isMagicBasedMelee(wep) && InteractionUtils.approxDistanceTo(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation()) > 3)
                        {
                            break;
                        }
                        if (config.prayMeleeAgainstStaff() && InteractionUtils.approxDistanceTo(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation()) < 2)
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
                        if (config.ignoreMeleeOutsideInstantRange() && InteractionUtils.approxDistanceTo(client.getLocalPlayer().getWorldLocation(), p.getWorldLocation()) > 3)
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

    private boolean opponentTracked(String name)
    {
        return opponents.getOrDefault(name, null) != null;
    }

    private boolean isMagicBasedMelee(Weapon wep)
    {
        return wep == Weapon.VOIDWAKER || wep == Weapon.SARADOMIN_SWORD;
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
