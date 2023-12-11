package com.lucidplugins.disablerendering;

import net.runelite.api.*;
import net.runelite.api.hooks.DrawCallbacks;

public class DisableRenderCallbacks implements DrawCallbacks
{
    public DisableRenderCallbacks()
    {
    }

    public void draw(Renderable renderable, int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash)
    {
    }

    public void drawScenePaint(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, SceneTilePaint paint, int tileZ, int tileX, int tileY, int zoom, int centerX, int centerY)
    {
    }

    public void drawSceneModel(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, SceneTileModel model, int tileZ, int tileX, int tileY, int zoom, int centerX, int centerY)
    {
    }

    public void draw(int overlayColor)
    {
    }

    public void drawScene(int cameraX, int cameraY, int cameraZ, int cameraPitch, int cameraYaw, int plane)
    {
    }

    public void postDrawScene()
    {
    }

    public void animate(Texture texture, int diff)
    {
    }

    public void loadScene(Scene scene)
    {
    }

    public void swapScene(Scene scene)
    {
    }
}
