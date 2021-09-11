package com.hablapatabla.implingfinder.model;

import com.hablapatabla.implingfinder.ImplingFinderPlugin;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImplingFinderWorldMapPoint extends WorldMapPoint
{
    private final ImplingFinderPlugin plugin;
    private final BufferedImage worldImage;
    private final Point worldImagePoint;

    public ImplingFinderWorldMapPoint(final WorldPoint worldPoint, ImplingFinderPlugin plugin)
    {
        super(worldPoint, null);

        worldImage = new BufferedImage(plugin.getWorldMapImage().getWidth(), plugin.getWorldMapImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = worldImage.getGraphics();
        graphics.drawImage(plugin.getWorldMapImage(), 0, 0, null);
        graphics.drawImage(plugin.getWorldMapImage(), 0, 0, null);
        worldImagePoint = new Point(
                worldImage.getWidth(),
                worldImage.getHeight());

        this.plugin = plugin;
        this.setName("Impling");
        this.setSnapToEdge(true);
        this.setJumpOnClick(true);
        this.setImage(worldImage);
        this.setImagePoint(worldImagePoint);
    }

    @Override
    public void onEdgeSnap() {
        this.setImage(plugin.getWorldMapImage());
        this.setImagePoint(null);
    }

    @Override
    public void onEdgeUnsnap() {
        this.setImage(worldImage);
        this.setImagePoint(worldImagePoint);
    }
}
