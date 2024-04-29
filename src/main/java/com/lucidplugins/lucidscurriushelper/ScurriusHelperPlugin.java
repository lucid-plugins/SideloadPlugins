package com.lucidplugins.lucidscurriushelper;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.google.inject.Provides;
import com.lucidplugins.api.utils.InteractionUtils;
import com.lucidplugins.api.utils.NpcUtils;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(name = "<html><font color=\"#32CD32\">Lucid </font>Scurrius Helper</html>", description = "Dodges Scurrius' falling ceiling attack and re-attacks")
public class ScurriusHelperPlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ScurriusHelperConfig config;

    @Inject
    private ConfigManager configManager;

    @Getter
    private final Map<GraphicsObject, Integer> fallingCeilingToTicks = new HashMap<>();

    private static final int FALLING_CEILING_GRAPHIC = 2644;

    private static final int SCURRIUS = 7222;
    private static final int SCURRIUS_PUBLIC = 7221;

    private static final int DURATION = 9;

    private boolean justDodged = false;

    private int lastDodgeTick = 0;

    private int lastRatTick = 0;

    @Provides
    ScurriusHelperConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ScurriusHelperConfig.class);
    }

    @Subscribe
    private void onGraphicsObjectCreated(GraphicsObjectCreated event)
    {
        final GraphicsObject graphicsObject = event.getGraphicsObject();
        final int id = graphicsObject.getId();

        if (id == FALLING_CEILING_GRAPHIC)
        {
            fallingCeilingToTicks.put(graphicsObject, DURATION);
        }
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        WorldPoint instancePoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());

        if (instancePoint.getRegionID() != 13210 || instancePoint.getRegionX() < 23)
        {
            return;
        }

        justDodged = false;

        if (!fallingCeilingToTicks.isEmpty())
        {
            dodgeFallingCeiling();

            fallingCeilingToTicks.replaceAll((k, v) -> v - 1);
            fallingCeilingToTicks.values().removeIf(v -> v <= 0);
        }

        NPC scurrius = NpcUtils.getNearestNpc("Scurrius");

        if (!justDodged)
        {
            if (config.attackAfterDodge() && client.getLocalPlayer().getInteracting() != scurrius)
            {
                if (ticksSinceLastDodge() < 3)
                {
                    if (scurrius != null)
                    {
                        if (!config.prioritizeRats() || getEligibleRat() == null)
                        {
                            NpcUtils.attackNpc(scurrius);
                        }
                    }
                }
            }
        }

        boolean attackRat = true;

        if (scurrius != null)
        {
            int ratio = scurrius.getHealthRatio();
            int scale = scurrius.getHealthScale();

            double targetHpPercent = (double) ratio  / (double) scale * 100;

            if (targetHpPercent > 0)
            {
                attackRat = false;
            }
        }

        if (justDodged)
        {
            return;
        }

        if (config.attackRats() && attackRat || (config.prioritizeRats()))
        {
            NPC giantRat = getEligibleRat();
            if (giantRat != null && giantRat != client.getLocalPlayer().getInteracting())
            {
                NpcUtils.attackNpc(giantRat);
                lastRatTick = client.getTickCount();
            }
            else
            {
                if (config.prioritizeRats() && giantRat == null)
                {
                    if (scurrius != null && ticksSinceLatRatHit() < 8 && client.getLocalPlayer().getInteracting() != scurrius)
                    {
                        NpcUtils.attackNpc(scurrius);
                    }
                }
            }
        }
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event)
    {
        if (event.getNpc().getId() == SCURRIUS || event.getNpc().getId() == SCURRIUS_PUBLIC)
        {
            if (config.attackOnSpawn())
            {
                lastDodgeTick = client.getTickCount();
            }
        }
    }


    private void dodgeFallingCeiling()
    {
        for (Map.Entry<GraphicsObject, Integer> fallingCeiling : fallingCeilingToTicks.entrySet())
        {
            LocalPoint unsafeTile = fallingCeiling.getKey().getLocation();
            LocalPoint playerTile = client.getLocalPlayer().getLocalLocation();
            if (unsafeTile.getX() == playerTile.getX() && unsafeTile.getY() == playerTile.getY())
            {
                NPC scurrius = NpcUtils.getNearestNpc("Scurrius");
                if (scurrius != null)
                {
                    List<LocalPoint> unsafeTiles = fallingCeilingToTicks.keySet().stream().map(GraphicsObject::getLocation).collect(Collectors.toList());
                    WorldPoint safeTile = null;

                    if (config.stayMelee())
                    {
                        safeTile = InteractionUtils.getClosestSafeLocationInNPCMeleeDistance(unsafeTiles, scurrius);
                    }
                    else
                    {
                        safeTile = InteractionUtils.getClosestSafeLocationNotInNPCMeleeDistance(unsafeTiles, scurrius);

                    }

                    if (safeTile != null)
                    {
                        InteractionUtils.walk(safeTile);
                        justDodged = true;
                        lastDodgeTick = client.getTickCount();
                    }
                }
            }
        }
    }

    private int ticksSinceLastDodge()
    {
        return client.getTickCount() - lastDodgeTick;
    }

    private int ticksSinceLatRatHit()
    {
        return client.getTickCount() - lastRatTick;
    }

    private NPC getEligibleRat()
    {
        return NpcUtils.getNearestNpc(npc -> {
            if (npc == null)
            {
                return false;
            }
            int ratio = npc.getHealthRatio();
            int scale = npc.getHealthScale();

            double targetHpPercent = (double) ratio  / (double) scale * 100;
            return npc.getName() != null && npc.getName().equals("Giant rat") && targetHpPercent > 0;
        });
    }
}
