package com.gpl.rpg.atcontentstudio.ui.map;

import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.ui.tools.MatrixComposite;

import java.awt.*;

public class MapColorFilters {

    public static void applyColorfilter(TMXMap.ColorFilter colorFilter, Graphics2D g2d) {
        Composite oldComp = g2d.getComposite();
        Rectangle clip = g2d.getClipBounds();
        MatrixComposite newComp = null;
        float f;
        switch (colorFilter) {
            case black20:
                f = 0.8f;
                newComp = new MatrixComposite(new float[]{
                        f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case black40:
                f = 0.6f;
                newComp = new MatrixComposite(new float[]{
                        f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case black60:
                f = 0.4f;
                newComp = new MatrixComposite(new float[]{
                        f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case black80:
                f = 0.2f;
                newComp = new MatrixComposite(new float[]{
                        f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case bw:
                newComp = new MatrixComposite(new float[]{
                        0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
                        0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
                        0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case invert:
                newComp = new MatrixComposite(new float[]{
                        -1.00f, 0.00f, 0.00f, 0.0f, 255.0f,
                        0.00f, -1.00f, 0.00f, 0.0f, 255.0f,
                        0.00f, 0.00f, -1.00f, 0.0f, 255.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case redtint:
                newComp = new MatrixComposite(new float[]{
                        1.20f, 0.20f, 0.20f, 0.0f, 25.0f,
                        0.00f, 0.80f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.80f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case greentint:
                newComp = new MatrixComposite(new float[]{
                        0.85f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.15f, 1.15f, 0.15f, 0.0f, 15.0f,
                        0.00f, 0.00f, 0.85f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case bluetint:
                newComp = new MatrixComposite(new float[]{
                        0.70f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.70f, 0.00f, 0.0f, 0.0f,
                        0.30f, 0.30f, 1.30f, 0.0f, 40.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            case none:
                f = 1f;
                newComp = new MatrixComposite(new float[]{
                        f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                });
                break;
            default:
                break;

        }
        if (newComp != null) {
            g2d.setComposite(newComp);
            g2d.setPaint(new Color(1.0f, 1.0f, 1.0f, 1.0f));
            g2d.fill(clip);
            g2d.setComposite(oldComp);
        }
    }

}
