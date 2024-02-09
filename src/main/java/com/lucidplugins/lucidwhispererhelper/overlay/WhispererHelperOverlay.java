package com.lucidplugins.lucidwhispererhelper.overlay;

import com.lucidplugins.lucidwhispererhelper.WhispererHelperConfig;
import com.lucidplugins.lucidwhispererhelper.WhispererHelperPlugin;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.util.Collection;
import java.util.Optional;

public class WhispererHelperOverlay extends Overlay
{

    private final Client client;
    private final WhispererHelperPlugin plugin;
    private final WhispererHelperConfig config;
    private Player player;
    @Inject
    WhispererHelperOverlay(final Client client, final WhispererHelperPlugin plugin, final WhispererHelperConfig config)
    {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics2D)
    {

        player = client.getLocalPlayer();

        if (player == null)
        {
            return null;
        }

        if (config.bindOverlay() && plugin.getBindTicks() > 0)
        {
            renderTileMarkerWorldPoint(client.getLocalPlayer().getWorldLocation(), graphics2D, plugin.getBindTicks() + "", Color.YELLOW);
        }

        if (plugin.getLeeches() != null && plugin.getLeeches().size() > 0)
        {
            renderLeechTiles(graphics2D);
        }

        if (plugin.getVitas() != null && plugin.getVitas().size() > 0)
        {
            renderVitaTiles(graphics2D);
        }

        if (config.pillarOverlay())
        {
            if (plugin.getMostHealthPillar() != null)
            {
                renderTileMarkerLocalPoint(plugin.getMostHealthPillar(), graphics2D, "3", Color.RED);
            }

            if (plugin.getNextMostHealthPillar() != null)
            {
                renderTileMarkerLocalPoint(plugin.getNextMostHealthPillar(), graphics2D, "2", Color.YELLOW);
            }

            if (plugin.getLeastHealthPillar() != null)
            {
                renderTileMarkerLocalPoint(plugin.getLeastHealthPillar(), graphics2D, "1", Color.GREEN);
            }
        }

        return null;
    }

    private void renderLeechTiles(Graphics2D graphics2D)
    {
        if (config.leechOverlay())
        {
            for (LocalPoint tile : plugin.getLeeches())
            {
                if (tile != null && tile.isInScene())
                {
                    renderTileMarkerLocalPoint(tile, graphics2D, "", Color.GREEN);
                }
            }
        }
    }

    private void renderVitaTiles(Graphics2D graphics2D)
    {
        if (config.vitaOverlay())
        {
            for (NPC vita : plugin.getVitas())
            {
                LocalPoint tile = vita.getLocalLocation();
                if (tile != null && tile.isInScene())
                {
                    renderTileMarkerLocalPoint(tile, graphics2D, "", Color.YELLOW);
                }
            }
        }
    }

    private void renderTileMarkerLocalPoint(LocalPoint lp, Graphics2D graphics2D, String text, Color color)
    {
        if (lp == null)
        {
            return;
        }

        final Polygon polygon = Perspective.getCanvasTileAreaPoly(client, lp, 1);
        if (polygon == null)
        {
            return;
        }

        final Point point = Perspective.getCanvasTextLocation(client, graphics2D, lp, text, -25);
        if (point == null)
        {
            return;
        }

        final Font originalFont = graphics2D.getFont();
        graphics2D.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 120);

        drawOutlineAndFill(graphics2D, null, fillColor, 2, polygon);
        OverlayUtil.renderTextLocation(graphics2D, point, text, Color.BLACK);
        graphics2D.setFont(originalFont);
    }

    private void renderTileMarkerWorldPoint(WorldPoint wp, Graphics2D graphics2D, String text, Color color)
    {
        if (wp == null)
        {
            return;
        }

        renderTileMarkerLocalPoint(LocalPoint.fromWorld(client, wp), graphics2D, text, color);
    }

    private WorldPoint getLocalInstancePoint(int regionX, int regionY)
    {
        WorldPoint wp = WorldPoint.fromRegion(10595, regionX, regionY, client.getLocalPlayer().getWorldLocation().getPlane());
        Collection<WorldPoint> localInstanceWp = WorldPoint.toLocalInstance(client, wp);
        Optional<WorldPoint> instanceWp = localInstanceWp.stream().findFirst();

        return instanceWp.orElse(null);
    }
}
