package com.lucidplugins.lucidwildytele;

import com.google.inject.Provides;
import com.lucidplugins.api.utils.InventoryUtils;
import com.lucidplugins.api.utils.PlayerUtils;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.kit.KitType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#32CD32\">Lucid </font>Wildy Tele</html>",
        description = "Teleports away from potential pkers the instant they appear using the Seed Pod. Other options coming soon.",
        enabledByDefault = false,
        tags = {"tele", "lucid", "wildy"}
)
public class LucidWildyTelePlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    private LucidWildyTeleConfig config;

    @Inject
    private ConfigManager configManager;


    @Override
    protected void startUp()
    {

    }

    @Override
    protected void shutDown()
    {

    }

    @Provides
    LucidWildyTeleConfig getConfig(final ConfigManager configManager)
    {
        return configManager.getConfig(LucidWildyTeleConfig.class);
    }


    @Subscribe
    private void onGameTick(final GameTick tick)
    {
        if (!config.ignoreWildyLevel())
        {
            int wildyLevel = Util.getWildernessLevelFrom(client.getLocalPlayer().getWorldLocation());
            if (wildyLevel == 0 || wildyLevel > 30)
            {
                return;
            }
        }

        if (playerInMyBracketSkulledWithNoAvariceOrPneck() || (config.alwaysTele() && playerInMyBracket()))
        {
            teleSeedPod();
        }
        else if (config.ignoreWildyLevel() && playerAroundUs())
        {
            teleSeedPod();
        }
    }

    private boolean playerInMyBracketSkulledWithNoAvariceOrPneck()
    {
        Player skulledPlayerWithoutAvariceorPneck = PlayerUtils.getNearest(player -> player != client.getLocalPlayer() && Util.isAttackable(client, player, config.ignoreWildyLevel()) &&
                player.getSkullIcon() != -1 &&
                !wearingAvariceOrPhoenixNeck(player));

        return skulledPlayerWithoutAvariceorPneck != null;
    }

    private boolean playerInMyBracket()
    {
        Player attackablePlayer = PlayerUtils.getNearest(player -> player != client.getLocalPlayer() && Util.isAttackable(client, player, config.ignoreWildyLevel()));

        return attackablePlayer != null;
    }

    private boolean playerAroundUs()
    {
        Player attackablePlayer = PlayerUtils.getNearest(player -> player != client.getLocalPlayer());

        return attackablePlayer != null;
    }

    private void teleSeedPod()
    {
        Item seedPod = InventoryUtils.getFirstItem("Royal seed pod");

        if (seedPod != null && client.getLocalPlayer().getAnimation() != 4544)
        {
            InventoryUtils.itemInteract(seedPod.getId(), "Commune");
        }
    }

    private boolean wearingAvariceOrPhoenixNeck(Player player)
    {
        if (player.getPlayerComposition() == null)
        {
            return false;
        }

        int id = player.getPlayerComposition().getEquipmentId(KitType.AMULET);

        if (id == ItemID.AMULET_OF_AVARICE)
        {
            return true;
        }
        else if (id == ItemID.PHOENIX_NECKLACE)
        {
            return true;
        }

        return false;
    }
}