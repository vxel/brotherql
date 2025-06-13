package org.delaunois.brotherql.util;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Various image converters and utility functions.
 */
public class Converter {
    static class C3 {
        private int r, g, b;

        public C3(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public C3 add(C3 o) {
            return new C3(r + o.r, g + o.g, b + o.b);
        }

        public int clamp(int c) {
            return Math.max(0, Math.min(255, c));
        }

        public int diff(C3 o) {
            int Rdiff = o.r - r;
            int Gdiff = o.g - g;
            int Bdiff = o.b - b;
            return Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
        }

        public C3 mul(double d) {
            return new C3((int) (d * r), (int) (d * g), (int) (d * b));
        }

        public C3 sub(C3 o) {
            return new C3(r - o.r, g - o.g, b - o.b);
        }

        public Color toColor() {
            return new Color(clamp(r), clamp(g), clamp(b));
        }

    }

    private static C3 findClosestPaletteColor(C3 c, C3[] palette) {
        C3 closest = palette[0];

        for (C3 n : palette) {
            if (n.diff(c) < closest.diff(c)) {
                closest = n;
            }
        }

        return closest;
    }

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
     * Convert the sRGB image to monochrome black and white using the Floyd-Steinberg dithering algorithm.
     *
     * @param img        the image to dither
     * @param brightness the brightness factor to apply before dithering.
     * @return the dithered image
     */
    public static BufferedImage floydSteinbergDithering(BufferedImage img, float brightness) {
        C3[] palette = new C3[]{
                new C3(0, 0, 0), // black
                new C3(255, 255, 255)  // white
        };

        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dithered = new BufferedImage(w, h, img.getType());

        C3[][] d = new C3[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int lum = luminance(img.getRGB(x, y));
                lum = Math.max(0, Math.min(255, (int) (lum * brightness)));
                d[y][x] = new C3(lum, lum, lum);
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                C3 oldColor = d[y][x];
                C3 newColor = findClosestPaletteColor(oldColor, palette);
                dithered.setRGB(x, y, newColor.toColor().getRGB());

                C3 err = oldColor.sub(newColor);

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
     *
     * @param img       the image to convert
     * @param threshold the threshold value (between 0 and 1) to discriminate between black and white pixels.
     * @return the converted image
     */
    public static BufferedImage threshold(BufferedImage img, float threshold) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage converted = new BufferedImage(w, h, img.getType());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float lum = luminance(img.getRGB(x, y)) / 255.0f;
                converted.setRGB(x, y, lum > threshold ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
        return converted;
    }
    
    /**
     * Rotate the given image by a multiple of 90 degrees (e.g. -270, -90, 90, 180, 270).
     * 
     * @param src the image to rotate
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
     * @param img the image to scale
     * @param width the target width
     * @param height the target height
     * @return the scaled image
     */
    public static BufferedImage scale(BufferedImage img, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.clearRect(0, 0, width, height);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }    
    
}