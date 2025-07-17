package org.delaunois.brotherql.backend;

import org.delaunois.brotherql.BrotherQLConnection;
import org.delaunois.brotherql.BrotherQLJob;
import org.delaunois.brotherql.BrotherQLMedia;
import org.delaunois.brotherql.example.PrintExample;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class BrotherQLDeviceFileTest {

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
        
        BrotherQLConnection connection = new BrotherQLConnection("file://white-dove-306.bin");
        connection.open();
        connection.sendJob(job);
        connection.close();
    }

    
}
