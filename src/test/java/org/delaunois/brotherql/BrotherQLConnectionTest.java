package org.delaunois.brotherql;

import org.delaunois.brotherql.backend.BrotherQLDeviceSimulator;
import org.delaunois.brotherql.example.PrintExample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class BrotherQLConnectionTest {

    private BrotherQLDeviceSimulator deviceSimulator;
    private BrotherQLConnection connection;

    @Before
    public void setUp() throws BrotherQLException {
        // Simulate a Brother QL-700 model with 62mm continuous labels
        BrotherQLModel simulatedModel = BrotherQLModel.QL_700_P;
        BrotherQLMedia simulatedMedia = BrotherQLMedia.CT_62_720; // Die-cut label, 62x100mm

        deviceSimulator = new BrotherQLDeviceSimulator(simulatedModel, simulatedMedia);
        connection = new BrotherQLConnection(deviceSimulator);
        connection.open();
    }

    @After
    public void tearDown() {
        connection.close();
        assertTrue(deviceSimulator.isClosed());
    }

    @Test
    public void testGetModel() {
        BrotherQLModel model = connection.getModel();
        assertNotNull("Model should not be null", model);
        assertEquals("Expected model is Brother QL-700", BrotherQLModel.QL_700_P, model);
    }

    @Test
    public void testReadStatus() {
        BrotherQLStatus status = connection.readDeviceStatus();
        assertNotNull("Status should not be null", status);

        // Verify specific parts of the status
        assertEquals(62, status.getMediaWidth()); // Label width
        assertEquals(0, status.getMediaLength()); // Label length (0 = continuous)
        assertEquals(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE.code, status.getMediaType().code); // Media type
    }

    @Test
    public void testSetMediaAndReadStatus() {
        // Update the media in the simulator
        BrotherQLMedia newMedia = BrotherQLMedia.CT_29_720; // 29mm continuous tape
        deviceSimulator.setMedia(newMedia);

        BrotherQLStatus status = connection.readDeviceStatus();
        assertNotNull(status);
        assertEquals("Updated media width should be 29mm", 29, status.getMediaWidth());
        assertEquals("Updated media length for continuous roll should be 0", 0, status.getMediaLength());
        assertEquals(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, status.getMediaType());
    }
    
    @Test
    public void testSimulateErrorStatus() {
        deviceSimulator.setErrorInfo1((byte) 0x01); // Example error: No media
        deviceSimulator.setErrorInfo2((byte) 0x02); // Secondary error

        BrotherQLStatus status = connection.readDeviceStatus();
        assertNotNull(status);
        EnumSet<BrotherQLErrorType> errors = status.getErrors();
        assertTrue("Error set should contain NO_MEDIA_WHEN_PRINTING", 
                   errors.contains(BrotherQLErrorType.NO_MEDIA_WHEN_PRINTING));
    }

    @Test
    public void testStatusParsing() {
        ByteBuffer buffer = deviceSimulator.readStatus(1000);
        byte[] status = new byte[32];
        buffer.get(status);
        BrotherQLStatus parsedStatus = new BrotherQLStatus(status, deviceSimulator.getModel());

        assertEquals("Parsed media width should match", 62, parsedStatus.getMediaWidth());
        assertEquals("Parsed media length should match", 0, parsedStatus.getMediaLength());
        assertEquals("Parsed media type should match",
                BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, parsedStatus.getMediaType());
        assertEquals("Parsed printer status type should be READY",
                BrotherQLStatusType.READY, parsedStatus.getStatusType());
    }
    
    @Test
    public void testSendJob() throws IOException, BrotherQLException {
        InputStream is = PrintExample.class.getResourceAsStream("/david.png");
        InputStream rasterIs = PrintExample.class.getResourceAsStream("/david-job.raster");
        String raster = new String(Objects.requireNonNull(rasterIs).readAllBytes());
        BufferedImage img = ImageIO.read(Objects.requireNonNull(is));
        
        BrotherQLJob job = new BrotherQLJob()
                .setAutocut(true)
                .setBrightness(1.0f)
                .setImages(List.of(img));
                        
        connection.sendJob(job);

        assertEquals(raster, deviceSimulator.getTx());
    }    
    
}