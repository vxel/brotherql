package org.delaunois.brotherql.backend;

import lombok.Getter;
import org.delaunois.brotherql.BrotherQLMedia;
import org.delaunois.brotherql.BrotherQLPhaseType;
import org.delaunois.brotherql.BrotherQLModel;
import org.delaunois.brotherql.BrotherQLStatusType;
import org.delaunois.brotherql.util.Hex;
import org.usb4java.BufferUtils;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A dummy implementation of BrotherQLDevice interface
 *
 * @author Cedric de Launois
 */
@SuppressWarnings("unused")
public class BrotherQLDeviceSimulator implements BrotherQLDevice {
    
    private static final Logger LOGGER = System.getLogger(BrotherQLDeviceSimulator.class.getName());
    
    private byte[] status = new byte[] {
            (byte)0x80, 0x20, 0x42, 0x34, 0x35, 0x30, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0, 
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };
    
    private BrotherQLModel model;

    /**
     * A formatted string showing the bytes received by the simulator
     */
    @Getter
    private String tx = "";

    /**
     * Simulate a brother QL printer with the given id and given media.
     * 
     * @param model the printer model
     * @param media the media
     */
    public BrotherQLDeviceSimulator(BrotherQLModel model, BrotherQLMedia media) {
        this.model = model;
        status[10] = (byte)media.labelWidthMm;
        status[17] = (byte)media.labelLengthMm;
        status[11] = media.mediaType.code;
    }

    /**
     * Change the media.
     * 
     * @param media the new media
     */
    public void setMedia(BrotherQLMedia media) {
        status[10] = (byte)media.labelWidthMm;
        status[17] = (byte)media.labelLengthMm;
        status[11] = media.mediaType.code;
    }

    /**
     * Set another status type for the simulated printer.
     * 
     * @param statusType the new status type
     */
    public void setStatusType(BrotherQLStatusType statusType) {
        status[18] = statusType.code;
    }

    /**
     * Set another phase type for the simulated printer.
     * 
     * @param phaseType the new status type
     */
    public void setPhaseType(BrotherQLPhaseType phaseType) {
        status[19] = phaseType.code;
    }

    /**
     * Set the first error byte for the simulated printer.
     * 
     * @param error the error byte
     */
    public void setErrorInfo1(byte error) {
        status[8] = error;
    }

    /**
     * Set the second error byte for the simulated printer.
     * 
     * @param error the error byte
     */
    public void setErrorInfo2(byte error) {
        status[9] = error;
    }

    private boolean open = false;
    
    @Override
    public void open() {
        open = true;
    }

    @Override
    public BrotherQLModel getModel() {
        return model;
    }

    @Override
    public ByteBuffer readStatus(long timeout) {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(32).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(status);
        buffer.rewind();
        LOGGER.log(Level.INFO, "Rx: " + Hex.toString(status));
        return buffer;
    }

    @Override
    public void write(byte[] data, long timeout) {
        LOGGER.log(Level.INFO, "Tx: " + Hex.toString(data));
        tx += Hex.toString(data) + "\n";
    }

    @Override
    public boolean isClosed() {
        return !open;
    }

    @Override
    public void close() {
        open = false;
    }

    /**
     * Clears the bytes received by the simulator.
     */
    public void clearTx() {
        tx = "";
    }
}
