package com.skeldoor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class DuckOverlay extends Overlay{
    public static final int IMAGE_Z_OFFSET = 30;

    private final DuckPlugin plugin;

    @Inject
    public DuckOverlay(DuckPlugin plugin)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        for (Duck duck : plugin.ducks){
            if (duck.getQuacking())
            {
                LocalPoint lp =  duck.getLocalLocation();
                if (lp != null)
                {
                    int duckHeight = 37;
                    Point p = Perspective.localToCanvas(plugin.getClient(), lp, plugin.getClient().getPlane(), duckHeight);
                    if (p != null)
                    {
                        Font overheadFont = FontManager.getRunescapeBoldFont();
                        FontMetrics metrics = graphics.getFontMetrics(overheadFont);
                        String quackText = duck.getQuackText();
                        Point shiftedP = new Point(p.getX() - (metrics.stringWidth(quackText) / 2), p.getY());

                        graphics.setFont(overheadFont);
                        OverlayUtil.renderTextLocation(graphics, shiftedP, quackText,
                                JagexColors.YELLOW_INTERFACE_TEXT);
                    }
                }
            }
        }

        return null;
    }
}
