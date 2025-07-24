package org.delaunois.brotherql.example;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.BrotherQLJob;
import org.delaunois.brotherql.BrotherQLMedia;
import org.delaunois.brotherql.util.Converter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Example of label rastering options.
 * The code can be used to preview the label rendering, given the job options.
 *
 * @author Cedric de Launois
 */
public class RasterExample {
    
    private static final System.Logger LOGGER = System.getLogger(RasterExample.class.getName());

    public static void main(String[] args) throws IOException {
        rasterWithMonochromeDithering();
        rasterWithBlackRedDithering();
        rasterGradientWithBlackRedDithering();
        rasterWithBlackRedThreshold();
        rasterWithThreshold();
        extractColorLayers();
    }

    public static void rasterWithMonochromeDithering() throws IOException {
        File outputfile;
        BrotherQLJob job;
        List<BufferedImage> rastered;

        // Use image dithering (Floyd-Steinberg)
        job = new BrotherQLJob()
                .setDither(true)
                .setBrightness(1.0f)
                .setImages(List.of(loadImage("/white-dove-696.png")));
        
        rastered = BrotherQLConnection.raster(job);
        outputfile = new File("white-dove-696-dither.png");
        ImageIO.write(rastered.get(0), "png", outputfile);
    }
    
    public static void rasterWithBlackRedDithering() throws IOException {
        File outputfile;
        BrotherQLJob job;
        List<BufferedImage> rastered;

        // Use image dithering (Floyd-Steinberg)
        job = new BrotherQLJob()
                .setDither(true)
                .setMedia(BrotherQLMedia.CT_62_720_BLACK_RED)
                .setBrightness(1.0f)
                .setImages(List.of(loadImage("/white-dove-696rb.png")));
        
        rastered = BrotherQLConnection.raster(job);
        outputfile = new File("white-dove-696rb-dither-rb.png");
        ImageIO.write(rastered.get(0), "png", outputfile);
    }

    public static void rasterGradientWithBlackRedDithering() throws IOException {
        File outputfile;
        BrotherQLJob job;
        List<BufferedImage> rastered;

        // Use image dithering (Floyd-Steinberg)
        job = new BrotherQLJob()
                .setDither(true)
                .setMedia(BrotherQLMedia.CT_62_720_BLACK_RED)
                .setBrightness(1.0f)
                .setImages(List.of(loadImage("/two-color-gradient-696.png")));
        
        rastered = BrotherQLConnection.raster(job);
        outputfile = new File("two-color-gradient-696-dither-rb.png");
        ImageIO.write(rastered.get(0), "png", outputfile);
    }

    public static void rasterWithBlackRedThreshold() throws IOException {
        File outputfile;
        BrotherQLJob job;
        List<BufferedImage> rastered;

        // Use image dithering (Floyd-Steinberg)
        job = new BrotherQLJob()
                .setDither(false)
                .setThreshold(0.5f)
                .setMedia(BrotherQLMedia.CT_62_720_BLACK_RED)
                .setBrightness(1.0f)
                .setImages(List.of(loadImage("/two-color-gradient-696.png")));
        
        rastered = BrotherQLConnection.raster(job);
        outputfile = new File("two-color-gradient-696-threshold-rb.png");
        ImageIO.write(rastered.get(0), "png", outputfile);
    }

    public static void rasterWithThreshold() throws IOException {
        File outputfile;
        BrotherQLJob job;
        List<BufferedImage> rastered;

        // Use threshold
        job = new BrotherQLJob()
                .setDither(false)
                .setRotate(-90)
                .setThreshold(0.7f)
                .setImages(List.of(loadImage("/white-dove-696.png")));
        
        rastered = BrotherQLConnection.raster(job);
        outputfile = new File("white-dove-696-threshold.png");
        ImageIO.write(rastered.get(0), "png", outputfile);
    }
    
    public static void extractColorLayers() throws IOException {
        BufferedImage image = loadImage("/white-dove-696rb.png");
        BufferedImage[] layers = Converter.extractLayer(image,
                color -> color.r == 255 && Math.abs(color.g - color.b) < 10);    

        for (int i = 0; i < layers.length; i++) {
            BufferedImage layer = layers[i];
            File outputfile = new File("white-dove-696rb-layer-" + i + ".png");
            ImageIO.write(layer, "png", outputfile);
        }
    }

    private static BufferedImage loadImage(String path) throws IOException {
        InputStream is = RasterExample.class.getResourceAsStream(path);
        if (is == null) {
            LOGGER.log(System.Logger.Level.INFO, "Resource not found");
            throw  new IOException("Resource not found");
        }
        
        return ImageIO.read(is);
    }
    
}
