package com.lucidplugins.lucidhotkeys2;

import com.example.EthanApiPlugin.EthanApiPlugin;
import lombok.Getter;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.Collection;

@Getter
public class LocalRegionTile
{

    private LocalPoint localTile;
    private int regionId;
    private int lpX;
    private int lpY;

    public LocalRegionTile(int regionId, int x, int y)
    {
        this.regionId = regionId;
        this.lpX = x;
        this.lpY = y;
        localTile = getInstanceLocalPoint(regionId, x, y);
    }

    public static LocalPoint getInstanceLocalPoint(int regionId, int x, int y)
    {
        final Collection<WorldPoint> worldPoints = WorldPoint.toLocalInstance(EthanApiPlugin.getClient(),
                WorldPoint.fromRegion(regionId, x, y, EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation().getPlane()));

        final WorldPoint worldPoint = worldPoints.stream().findFirst().orElse(null);

        if (worldPoint == null)
        {
            return null;
        }

        return LocalPoint.fromWorld(EthanApiPlugin.getClient(), worldPoint);
    }

    public LocalPoint getLocalTile()
    {
        localTile = getInstanceLocalPoint(regionId, lpX, lpY);
        return localTile;
    }
}
