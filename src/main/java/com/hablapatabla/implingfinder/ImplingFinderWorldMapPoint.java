package com.hablapatabla.implingfinder;

import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

import java.awt.*;
import java.awt.image.BufferedImage;

class ImplingFinderWorldMapPoint extends WorldMapPoint
{
    private final ImplingFinderPlugin plugin;
    private final BufferedImage worldImage;
    private final Point worldImagePoint;

    ImplingFinderWorldMapPoint(final WorldPoint worldPoint, ImplingFinderPlugin plugin)
    {
        super(worldPoint, null);

        worldImage = new BufferedImage(plugin.getMapArrow().getWidth(), plugin.getMapArrow().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = worldImage.getGraphics();
        graphics.drawImage(plugin.getMapArrow(), 0, 0, null);
        graphics.drawImage(plugin.getClueScrollImage(), 0, 0, null);
        worldImagePoint = new Point(
                worldImage.getWidth() / 2,
                worldImage.getHeight());

        this.plugin = plugin;
        this.setSnapToEdge(true);
        this.setJumpOnClick(true);
        //this.setName("Clue Scroll");
        this.setImage(worldImage);
        this.setImagePoint(worldImagePoint);
    }

    @Override
    public void onEdgeSnap()
    {
        this.setImage(plugin.getClueScrollImage());
        this.setImagePoint(null);
    }

    @Override
    public void onEdgeUnsnap()
    {
        this.setImage(worldImage);
        this.setImagePoint(worldImagePoint);
    }
}
