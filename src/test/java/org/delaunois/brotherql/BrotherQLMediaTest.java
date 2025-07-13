package org.delaunois.brotherql;


import org.delaunois.brotherql.backend.BrotherQLDeviceTcp;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BrotherQLMediaTest {

    private static final System.Logger LOGGER = System.getLogger(BrotherQLDeviceTcp.class.getName());
    
    @Test
    public void testMediaCoherence() {
        Arrays.stream(BrotherQLMedia.values()).forEach(media -> {
            LOGGER.log(System.Logger.Level.INFO, "Testing " + media.name());
            assertEquals(media.rgtSizeBytes * 8, media.leftMarginPx + media.bodyWidthPx + media.rightMarginPx);
        });
    }
}
