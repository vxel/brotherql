package org.delaunois.brotherql.example;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.BrotherQLJob;
import org.delaunois.brotherql.BrotherQLMedia;

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
        rasterWithThreshold();
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
        outputfile = new File("dithered.png");
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
                .setImages(List.of(loadImage("/two-color-696.png")));
        
        rastered = BrotherQLConnection.raster(job);
        outputfile = new File("dithered-2color.png");
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
        outputfile = new File("thresholded.png");
        ImageIO.write(rastered.get(0), "png", outputfile);
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
