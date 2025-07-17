package com.gpl.rpg.atcontentstudio.utils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public final class SpriteUtils {

    /**
     * Check if the image is empty (transparent )
     *
     * @param img The image to check
     * @return true if the image is empty
     */
    public static boolean checkIsImageEmpty(BufferedImage img) {
        int width = img.getWidth(null);
        int height = img.getHeight(null);
        WritableRaster raster = img.getAlphaRaster();
        if (raster == null) {
            return false;
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //get pixel alpha value
                int alpha = raster.getSample(x, y, 0);
                //if alpha is not 0 then the pixel is not transparent
                if (alpha != 0) {
                    return false;
                }
            }
        }
        //no non-transparent pixel found
        return true;
    }
}
