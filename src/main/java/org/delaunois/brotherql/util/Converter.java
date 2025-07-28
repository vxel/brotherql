package org.delaunois.brotherql.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Predicate;

/**
 * Various image converters and utility functions.
 */
public class Converter {

    /**
     * A helper class for parsing and converting ARGB-encoded colors. 
     */
    public static class ARGB {

        /**
         * The alpha component.
         */
        public int a;

        /**
         * The red component.
         */
        public int r;

        /**
         * The green component.
         */
        public int g;

        /**
         * The blue component.
         */
        public int b;

        /**
         * Creates a new ARGB instance by parsing the ARGB-encoded color.
         * @param argb the ARGB-encoded color
         */
        public ARGB(int argb) {
            this.a = (argb >> 24) & 0xFF;
            this.r = (argb >> 16) & 0xFF;
            this.g = (argb >> 8) & 0xFF;
            this.b = argb & 0xFF;
        }

        /**
         * Creates a new ARGB instance using the given reg, green and blue components.
         * Alpha is set to 255 (no transparency).
         * 
         * @param r the red component, between 0 and 255 (0xFF)
         * @param g the green component, between 0 and 255 (0xFF)
         * @param b the blue component, between 0 and 255 (0xFF)
         */
        public ARGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = 255;
        }

        /**
         * Add the given color.
         * 
         * @param o the color to add
         * @return a new color, addition of this color and o.
         */
        public ARGB add(ARGB o) {
            return new ARGB(r + o.r, g + o.g, b + o.b);
        }

        /**
         * Subtract the given color.
         * 
         * @param o the color to substract
         * @return a new color, substraction of o from this color.
         */
        public ARGB sub(ARGB o) {
            return new ARGB(r - o.r, g - o.g, b - o.b);
        }

        /**
         * Computes the distance with the given color.
         * 
         * @param o the color
         * @return the distance.
         */
        public int diff(ARGB o) {
            int Rdiff = o.r - r;
            int Gdiff = o.g - g;
            int Bdiff = o.b - b;
            return Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
        }

        /**
         * Multiply with the given factor.
         * 
         * @param d the multiplication factor
         * @return a new color, multiplication of this color by d.
         */
        public ARGB mul(double d) {
            return new ARGB((int) (d * r), (int) (d * g), (int) (d * b));
        }

        /**
         * Convert the color to Color.
         * @return the converted Color
         */
        public Color toColor() {
            return new Color(clamp(r), clamp(g), clamp(b), clamp(a));
        }

        /**
         * Convert the color to a ARGB-encoded integer.
         * @return the ARGB-encoded integer
         */
        public int toRGB() {
            return a << 24 | (r << 16) | (g << 8) | b;
        }

        private int clamp(int c) {
            return Math.max(0, Math.min(255, c));
        }
        
    }

    /**
     * A 2-color palette consisting of black and white colors.
     */
    public static final ARGB[] PALETTE_BLACK_WHITE = new ARGB[]{
            new ARGB(0, 0, 0), // black
            new ARGB(255, 255, 255)  // white
    };

    /**
     * A 2-color palette consisting of red and white colors.
     */
    public static final ARGB[] PALETTE_RED_WHITE = new ARGB[]{
            new ARGB(255, 0, 0), // red
            new ARGB(255, 255, 255)  // white
    };

    private Converter() {
        // Prevent instanciation
    }

    /**
     * Compute the luminance of a given color.
     *
     * @param color the color (sRGB)
     * @return the luminance
     */
    public static int luminance(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return (int) (r * 0.299 + g * 0.587 + b * 0.114);
    }

    /**
     * Convert the sRGB image to the given palette using the Floyd-Steinberg dithering algorithm.
     *
     * @param img the image to dither
     * @param palette the palette to use
     * @return the dithered image
     */
    public static BufferedImage floydSteinbergDithering(BufferedImage img, ARGB[] palette) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dithered = new BufferedImage(w, h, img.getType());

        ARGB[][] d = toARGB(img);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                ARGB oldColor = d[y][x];
                ARGB newColor = findClosestPaletteColor(oldColor, palette);
                dithered.setRGB(x, y, newColor.toColor().getRGB());

                ARGB err = oldColor.sub(newColor);

                if (x + 1 < w) {
                    d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 16));
                }

                if (x - 1 >= 0 && y + 1 < h) {
                    d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
                }

                if (y + 1 < h) {
                    d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 16));
                }

                if (x + 1 < w && y + 1 < h) {
                    d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
                }
            }
        }

        return dithered;
    }

    /**
     * Convert the sRGB image to monochrome black and white using a luminance threshold.
     * Identical to a call to <code>threshold(img, threshold, Color.BLACK, Color.WHITE)</code>.
     *
     * @param img       the image to convert
     * @param threshold the threshold value (between 0 and 1) to discriminate between black and white pixels.
     * @return the converted image
     */
    public static BufferedImage threshold(BufferedImage img, float threshold) {
        return threshold(img, threshold, Color.BLACK, Color.WHITE);
    }

    /**
     * Convert the sRGB image to a 2-color palette using a luminance threshold.
     *
     * @param img       the image to convert
     * @param threshold the threshold value (between 0 and 1) to discriminate between low and high pixels.
     * @param low       the low color used when the luminance is below the threshold
     * @param high      the high color used when the luminance is above the threshold
     * @return the converted image
     */
    public static BufferedImage threshold(BufferedImage img, float threshold, Color low, Color high) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage converted = new BufferedImage(w, h, img.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float lum = luminance(img.getRGB(x, y)) / 255.0f;
                converted.setRGB(x, y, lum < threshold ? low.getRGB() : high.getRGB());
            }
        }
        return converted;
    }

    /**
     * Split image colors by extracting the pixels meeting the given condition.
     * The method returns 2 images in an array.
     * The first image returned contains all matching pixels, and pixels that do not match are Color.WHITE.
     * The second image returned contains pixels that do not match, and pixels that match are Color.WHITE.
     *
     * @param img       the image
     * @param condition the condition, a lambda.
     * @return 2 images : the matching image at index 0, the remaining image at index 1
     */
    public static BufferedImage[] extractLayer(BufferedImage img, Predicate<ARGB> condition) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage[] layers = new BufferedImage[2];
        BufferedImage matchingLayer = new BufferedImage(w, h, img.getType());
        BufferedImage remainingLayer = new BufferedImage(w, h, img.getType());
        layers[0] = matchingLayer;
        layers[1] = remainingLayer;

        ARGB[][] c3 = toARGB(img);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                ARGB color = c3[y][x];
                if (condition.test(color)) {
                    remainingLayer.setRGB(x, y, Color.WHITE.getRGB());
                    matchingLayer.setRGB(x, y, color.toColor().getRGB());
                } else {
                    remainingLayer.setRGB(x, y, color.toColor().getRGB());
                    matchingLayer.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        return layers;
    }

    /**
     * Merge the pixels of the given two images into a new image.
     * The pixels of the first image are overriden by non-white non-transparent pixels of the second image.
     *
     * @param first  the first image
     * @param second the second image
     * @return the merged new image
     */
    public static BufferedImage merge(BufferedImage first, BufferedImage second) {
        int w = first.getWidth();
        int h = first.getHeight();

        if (second.getWidth() != w || second.getHeight() != h) {
            throw new IllegalArgumentException("Images must have the same dimensions");
        }

        BufferedImage merged = new BufferedImage(w, h, first.getType());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = second.getRGB(x, y);
                if (pixel != 0 && pixel != Color.WHITE.getRGB()) {
                    merged.setRGB(x, y, pixel);
                } else {
                    merged.setRGB(x, y, first.getRGB(x, y));
                }
            }
        }
        return merged;
    }

    /**
     * Rotate the given image by a multiple of 90 degrees (e.g. -270, -90, 90, 180, 270).
     *
     * @param src   the image to rotate
     * @param angle the rotation angle, in degrees
     * @return the rotated image
     */
    public static BufferedImage rotate(BufferedImage src, int angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }

        if (angle != 90 && angle != 180 && angle != 270) {
            return src;
        }

        double theta = Math.toRadians(angle);
        int width = src.getWidth();
        int height = src.getHeight();

        BufferedImage dest;
        if (angle == 90 || angle == 270) {
            dest = new BufferedImage(src.getHeight(), src.getWidth(), src.getType());
        } else {
            dest = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        }

        Graphics2D graphics2D = dest.createGraphics();

        if (angle == 90) {
            graphics2D.translate((height - width) / 2, (height - width) / 2);
            graphics2D.rotate(theta, height / 2., width / 2.);
        } else if (angle == 270) {
            graphics2D.translate((width - height) / 2, (width - height) / 2);
            graphics2D.rotate(theta, height / 2., width / 2.);
        } else {
            graphics2D.translate(0, 0);
            graphics2D.rotate(theta, width / 2., height / 2.);
        }
        graphics2D.drawRenderedImage(src, null);
        return dest;
    }

    /**
     * Scale the image to the given size.
     *
     * @param img    the image to scale
     * @param width  the target width
     * @param height the target height
     * @return the scaled image
     */
    public static BufferedImage scale(BufferedImage img, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.clearRect(0, 0, width, height);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }

    /**
     * Remove the alpha channel of an image by blending it to a white background.
     *
     * @param image the image
     * @return the new image
     */
    public static BufferedImage removeAlpha(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage newImage = new BufferedImage(w, h, image.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                newImage.setRGB(x, y, rgba2rgb(image.getRGB(x, y)));
            }
        }
        return newImage;
    }

    /**
     * Apply a brigthness on the given image.
     *
     * @param image      the image
     * @param brightness the brightness factor, a positive float.
     * @return the new image
     */
    public static BufferedImage brightness(BufferedImage image, float brightness) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage newImage = new BufferedImage(w, h, image.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                ARGB argb = new ARGB(image.getRGB(x, y));
                argb.r = Math.min(255, (int) (argb.r * brightness));
                argb.g = Math.min(255, (int) (argb.g * brightness));
                argb.b = Math.min(255, (int) (argb.b * brightness));
                newImage.setRGB(x, y, argb.toRGB());
            }
        }
        return newImage;
    }

    /**
     * Remove the alpha channel of a pixel by blending it to a white background.
     *
     * @param rgba the color
     * @return the color without alpha
     */
    public static int rgba2rgb(int rgba) {
        ARGB argb = new ARGB(rgba);
        float alpha = argb.a / 255.0f;
        int blend = 255 - argb.a;
        argb.r = (int) (blend + alpha * argb.r);
        argb.g = (int) (blend + alpha * argb.g);
        argb.b = (int) (blend + alpha * argb.b);
        return argb.toRGB();
    }

    private static ARGB[][] toARGB(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        ARGB[][] d = new ARGB[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new ARGB(img.getRGB(x, y));
            }
        }
        return d;
    }

    private static ARGB findClosestPaletteColor(ARGB c, ARGB[] palette) {
        ARGB closest = palette[0];

        for (ARGB n : palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n;
            }
        }

        return closest;
    }
    
}