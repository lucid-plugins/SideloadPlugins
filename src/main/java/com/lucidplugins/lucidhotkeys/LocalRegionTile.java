package com.lucidplugins.lucidhotkeys;

import com.example.EthanApiPlugin.EthanApiPlugin;
import lombok.Getter;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.Collection;

@Getter
public class LocalRegionTile
{

    private LocalPoint localTile;
    private final int regionId;
    private final int lpX;
    private final int lpY;

    public LocalRegionTile(int regionId, int x, int y)
    {
        this.regionId = regionId;
        this.lpX = x;
        this.lpY = y;
        localTile = getInstanceLocalPoint(regionId, x, y);
    }

    public static LocalPoint getInstanceLocalPoint(int regionId, int x, int y)
    {
        final Collection<WorldPoint> worldPoints = WorldPoint.toLocalInstance(EthanApiPlugin.getClient().getTopLevelWorldView(),
                WorldPoint.fromRegion(regionId, x, y, EthanApiPlugin.getClient().getLocalPlayer().getWorldLocation().getPlane()));

        final WorldPoint worldPoint = worldPoints.stream().findFirst().orElse(null);

        if (worldPoint == null)
        {
            return null;
        }

        return LocalPoint.fromWorld(EthanApiPlugin.getClient().getTopLevelWorldView(), worldPoint);
    }

    public LocalPoint getLocalTile()
    {
        localTile = getInstanceLocalPoint(regionId, lpX, lpY);
        return localTile;
    }
}
