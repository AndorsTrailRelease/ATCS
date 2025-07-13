package com.gpl.rpg.atcontentstudio.ui.tools;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * This Composite emulates the behaviour of Android's ColorMatrixColorFilter,
 * except that you have to use this one a posteriori instead of a filtering paint.
 * <p>
 * It applies a ColorMatrix to the destination pixels, regardless of potential "source" pixels.
 * Once created and activated through Graphics2D.setComposite(), just paint anything over the pixels you want "filtered".
 * <p>
 * Works on a per-pixel basis, no sampling of surrounding pixels, or anything.
 *
 * @author pochat
 */
public class MatrixComposite implements Composite {


    final float[] matrix = new float[20];


    /**
     * Dismisses the source pixels. Just paint it black, or white, it only affects the dest RGB with the following formulae.
     * <p>
     * R' = a*R + b*G + c*B + d*A + e;
     * G' = f*R + g*G + h*B + i*A + j;
     * B' = k*R + l*G + m*B + n*A + o;
     * A' = p*R + q*G + r*B + s*A + t;
     *
     * @param matrix a flat float[20] array, giving the a..t values;
     */
    public MatrixComposite(float[] matrix) {
        if (matrix.length != this.matrix.length) {
            throw new Error("MatrixComposite matrix must be of length " + this.matrix.length);
        }
        System.arraycopy(matrix, 0, this.matrix, 0, this.matrix.length);
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel,
                                          ColorModel dstColorModel, RenderingHints hints) {
        return new MatrixCompositeContext(this);
    }

    class MatrixCompositeContext implements CompositeContext {

        MatrixComposite composite;

        public MatrixCompositeContext(MatrixComposite composite) {
            this.composite = composite;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            if (src.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                    dstIn.getSampleModel().getDataType() != DataBuffer.TYPE_INT ||
                    dstOut.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
                throw new IllegalStateException(
                        "Source and destination must store pixels as INT.");
            }

            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            float alpha = 1.0f;

            int[] srcPixel = new int[4];
            int[] dstPixel = new int[4];
            int[] srcPixels = new int[width];
            int[] dstPixels = new int[width];

            for (int y = 0; y < height; y++) {
                src.getDataElements(0, y, width, 1, srcPixels);
                dstIn.getDataElements(0, y, width, 1, dstPixels);
                for (int x = 0; x < width; x++) {
                    // pixels are stored as INT_ARGB
                    // our arrays are [R, G, B, A]
                    int pixel = srcPixels[x];
                    srcPixel[0] = (pixel >> 16) & 0xFF;
                    srcPixel[1] = (pixel >> 8) & 0xFF;
                    srcPixel[2] = (pixel) & 0xFF;
                    srcPixel[3] = (pixel >> 24) & 0xFF;

                    pixel = dstPixels[x];
                    dstPixel[0] = (pixel >> 16) & 0xFF;
                    dstPixel[1] = (pixel >> 8) & 0xFF;
                    dstPixel[2] = (pixel) & 0xFF;
                    dstPixel[3] = (pixel >> 24) & 0xFF;

                    int[] result = applyMatrix(matrix, dstPixel);

                    // mixes the result with the opacity
                    dstPixels[x] = ((int) (dstPixel[3] + (result[3] - dstPixel[3]) * alpha) & 0xFF) << 24 |
                            ((int) (dstPixel[0] + (result[0] - dstPixel[0]) * alpha) & 0xFF) << 16 |
                            ((int) (dstPixel[1] + (result[1] - dstPixel[1]) * alpha) & 0xFF) << 8 |
                            (int) (dstPixel[2] + (result[2] - dstPixel[2]) * alpha) & 0xFF;
                }
                dstOut.setDataElements(0, y, width, 1, dstPixels);
            }
        }

        private int[] applyMatrix(float[] matrix, int[] dstPixel) {
            int[] result = new int[4];
            result[0] = Math.max(0, Math.min(255, (int) (matrix[0] * dstPixel[0] + matrix[1] * dstPixel[1] + matrix[2] * dstPixel[2] + matrix[3] * dstPixel[3] + matrix[4])));
            result[1] = Math.max(0, Math.min(255, (int) (matrix[5] * dstPixel[0] + matrix[6] * dstPixel[1] + matrix[7] * dstPixel[2] + matrix[8] * dstPixel[3] + matrix[9])));
            result[2] = Math.max(0, Math.min(255, (int) (matrix[10] * dstPixel[0] + matrix[11] * dstPixel[1] + matrix[12] * dstPixel[2] + matrix[13] * dstPixel[3] + matrix[14])));
            result[3] = Math.max(0, Math.min(255, (int) (matrix[15] * dstPixel[0] + matrix[16] * dstPixel[1] + matrix[17] * dstPixel[2] + matrix[18] * dstPixel[3] + matrix[19])));
            return result;
        }

    }

}
