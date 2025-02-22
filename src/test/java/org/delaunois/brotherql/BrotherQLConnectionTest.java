package org.delaunois.brotherql;

import org.delaunois.brotherql.backend.BrotherQLDeviceSimulator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class BrotherQLConnectionTest {

    private BrotherQLDeviceSimulator deviceSimulator;
    private BrotherQLConnection connection;

    @Before
    public void setUp() throws BrotherQLException {
        // Simulate a Brother QL-700 model with 62mm continuous labels
        BrotherQLModel simulatedModel = BrotherQLModel.QL_700_P;
        BrotherQLMedia simulatedMedia = BrotherQLMedia.DC_62X100_720; // Die-cut label, 62x100mm

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
        assertEquals(100, status.getMediaLength()); // Label length
        assertEquals(BrotherQLMediaType.DIE_CUT_LABEL.code, status.getMediaType().code); // Media type
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
        assertEquals("Parsed media length should match", 100, parsedStatus.getMediaLength());
        assertEquals("Parsed media type should match",
                BrotherQLMediaType.DIE_CUT_LABEL, parsedStatus.getMediaType());
        assertEquals("Parsed printer status type should be READY",
                BrotherQLStatusType.READY, parsedStatus.getStatusType());
    }
    
}