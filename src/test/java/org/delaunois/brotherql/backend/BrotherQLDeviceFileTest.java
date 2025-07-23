package org.delaunois.brotherql.backend;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.BrotherQLJob;
import org.delaunois.brotherql.BrotherQLMedia;
import org.delaunois.brotherql.example.PrintExample;
import org.delaunois.brotherql.util.Hex;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

public class BrotherQLDeviceFileTest {

    private static final System.Logger LOGGER = System.getLogger(BrotherQLDeviceFileTest.class.getName());

    @Test
    public void testSendJob() throws Exception {
        InputStream is = PrintExample.class.getResourceAsStream("/white-dove-306.png");
        BufferedImage img = ImageIO.read(Objects.requireNonNull(is));
        
        BrotherQLJob job = new BrotherQLJob()
                .setAutocut(true)
                .setMedia(BrotherQLMedia.DC_29X90_720)
                .setAutocut(false)
                .setBrightness(1.0f)
                .setImages(List.of(img));
        
        BrotherQLConnection connection = new BrotherQLConnection("file:white-dove-306.bin?model=QL-820NWB");
        connection.open();
        connection.sendJob(job);
        connection.close();
        
        File bin = new File("white-dove-306.bin");
        assertTrue(bin.exists());

        LOGGER.log(System.Logger.Level.DEBUG, "File dump\n" + 
                Hex.prettyDump(Files.readAllBytes(Path.of(bin.getAbsolutePath()))));
    }

    
}
