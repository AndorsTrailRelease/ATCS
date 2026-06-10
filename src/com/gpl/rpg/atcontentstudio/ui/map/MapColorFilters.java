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

    public static float[] buildMatrixForFilter(com.gpl.rpg.atcontentstudio.model.maps.TMXMap.ColorFilter colorFilter) {
        float[] m;
        switch (colorFilter) {
            case black20:
                m = new float[]{
                        0.8f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.8f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.8f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case black40:
                m = new float[]{
                        0.6f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.6f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.6f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case black60:
                m = new float[]{
                        0.4f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.4f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.4f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case black80:
                m = new float[]{
                        0.2f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.2f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.2f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case bw:
                m = new float[]{
                        0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
                        0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
                        0.33f, 0.59f, 0.11f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case invert:
                m = new float[]{
                        -1.00f, 0.00f, 0.00f, 0.0f, 255.0f,
                        0.00f, -1.00f, 0.00f, 0.0f, 255.0f,
                        0.00f, 0.00f, -1.00f, 0.0f, 255.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case redtint:
                m = new float[]{
                        1.20f, 0.20f, 0.20f, 0.0f, 25.0f,
                        0.00f, 0.80f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.80f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case greentint:
                m = new float[]{
                        0.85f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.15f, 1.15f, 0.15f, 0.0f, 15.0f,
                        0.00f, 0.00f, 0.85f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case bluetint:
                m = new float[]{
                        0.70f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.70f, 0.00f, 0.0f, 0.0f,
                        0.30f, 0.30f, 1.30f, 0.0f, 40.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            case none:
                m = new float[]{
                        1f, 0.00f, 0.00f, 0.0f, 0.0f,
                        0.00f, 1f, 0.00f, 0.0f, 0.0f,
                        0.00f, 0.00f, 1f, 0.0f, 0.0f,
                        0.00f, 0.00f, 0.00f, 1.0f, 0.0f
                };
                break;
            default:
                return null;
        }
        return m;
    }

    public static void applyMatrixToImage(final float[] matrix, final java.awt.image.BufferedImage img) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        int[] pixels = img.getRGB(0, 0, w, h, null, 0, w);

        for (int i = 0; i < pixels.length; i++) {
            int px = pixels[i];
            int dstA = (px >> 24) & 0xFF;
            int dstR = (px >> 16) & 0xFF;
            int dstG = (px >> 8) & 0xFF;
            int dstB = px & 0xFF;

            int r = clamp((int) (matrix[0] * dstR + matrix[1] * dstG + matrix[2] * dstB + matrix[3] * dstA + matrix[4]));
            int g = clamp((int) (matrix[5] * dstR + matrix[6] * dstG + matrix[7] * dstB + matrix[8] * dstA + matrix[9]));
            int b = clamp((int) (matrix[10] * dstR + matrix[11] * dstG + matrix[12] * dstB + matrix[13] * dstA + matrix[14]));
            int a = clamp((int) (matrix[15] * dstR + matrix[16] * dstG + matrix[17] * dstB + matrix[18] * dstA + matrix[19]));

            pixels[i] = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
        }

        img.setRGB(0, 0, w, h, pixels, 0, w);
    }

    private static int clamp(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

}
