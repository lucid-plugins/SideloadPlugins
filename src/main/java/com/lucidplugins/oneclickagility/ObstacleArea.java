package com.lucidplugins.oneclickagility;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;


@Slf4j
public class ObstacleArea
{
    protected final int minX;
    protected final int maxX;
    protected final int minY;
    protected final int maxY;
    protected final int z;
    @Getter
    protected final int nextObstacleID;
    @Setter
    protected TileObject nextObstacle;

    ObstacleArea(int minX,int maxX, int minY, int maxY,int z, int nextObstacleID)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.z = z;
        this.nextObstacleID = nextObstacleID;
    }

    public boolean containsObject(TileObject locatable)
    {
        return containsObject(locatable.getWorldLocation());
    }

    public boolean containsObject(WorldPoint worldPoint)
    {
        return containsObject(worldPoint.getX(),worldPoint.getY(), worldPoint.getPlane());
    }

    protected boolean containsObject(int x, int y, int z)
    {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z == this.z;
    }

    public MenuEntry createMenuEntry(Client client)
    {
        if (nextObstacle != null)
        {
            return client.createMenuEntry(-1)
                    .setOption("Interact")
                    .setTarget("Obstacle")
                    .setIdentifier(nextObstacle.getId())
                    .setType(MenuAction.GAME_OBJECT_FIRST_OPTION)
                    .setParam0(getObjectParam(nextObstacle))
                    .setParam1(getObjectParam1(nextObstacle))
                    .setWorldViewId(client.getTopLevelWorldView().getId())
                    .setForceLeftClick(true);
        }
        return null;
    }

    protected int getObjectParam(TileObject gameObject)
    {
        if (gameObject instanceof GameObject)
        {
            return ((GameObject) gameObject).getSceneMinLocation().getX();
        }
        return(gameObject.getLocalLocation().getSceneX());
    }

    protected int getObjectParam1(TileObject gameObject)
    {
        if (gameObject instanceof GameObject)
        {
            return ((GameObject) gameObject).getSceneMinLocation().getY();
        }
        return(gameObject.getLocalLocation().getSceneY());
    }
}
