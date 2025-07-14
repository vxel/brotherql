package org.delaunois.brotherql.protocol;

/**
 * Constant for the QL Protocol.
 *
 * @author Cedric de Launois
 */
public class QL {

    /**
     * Length of a status message
     */
    public static final int STATUS_SIZE = 32;

    /**
     * Reset command
     */
    public static final byte[] CMD_RESET = new byte[400];

    /**
     * Initialize command
     */
    public static final byte[] CMD_INITIALIZE = new byte[]{0x1B, 0x40};

    /**
     * Status request command
     */
    public static final byte[] CMD_STATUS_REQUEST = new byte[]{0x1B, 0x69, 0x53};

    /**
     * Switch to raster command
     */
    public static final byte[] CMD_SWITCH_TO_RASTER = new byte[]{0x1B, 0x69, 0x61, 0x01};

    /**
     * Print information command
     */
    public static final byte[] CMD_PRINT_INFORMATION = new byte[]{0x1B, 0x69, 0x7A};

    /**
     * Set autocut ON command
     */
    public static final byte[] CMD_SET_AUTOCUT_ON = new byte[]{0x1B, 0x69, 0x4D, 0x40};

    /**
     * set autocut OFF command
     */
    public static final byte[] CMD_SET_AUTOCUT_OFF = new byte[]{0x1B, 0x69, 0x4D, 0};

    /**
     * Set cut pagenumber command
     */
    public static final byte[] CMD_SET_CUT_PAGENUMBER = new byte[]{0x1B, 0x69, 0x41};

    /**
     * Set expanded mode command
     */
    public static final byte[] CMD_SET_EXPANDED_MODE = new byte[]{0x1B, 0x69, 0x4B};

    /**
     * Set margin command
     */
    public static final byte[] CMD_SET_MARGIN = new byte[]{0x1B, 0x69, 0x64};

    /**
     * Raster graphic transfer command
     */
    public static final byte[] CMD_RASTER_GRAPHIC_TRANSFER = new byte[]{0x67, 0x00};

    /**
     * Two-color raster graphic transfer command first color
     */
    public static final byte[] CMD_TWO_COLOR_RASTER_GRAPHIC_TRANSFER_FIRST = new byte[]{0x77, 0x01};

    /**
     * Two-color raster graphic transfer command second color
     */
    public static final byte[] CMD_TWO_COLOR_RASTER_GRAPHIC_TRANSFER_SECOND = new byte[]{0x77, 0x02};

    /**
     * Print command
     */
    public static final byte[] CMD_PRINT = new byte[]{0x0C};

    /**
     * Print last page command
     */
    public static final byte[] CMD_PRINT_LAST = new byte[]{0x1A};

    /**
     * Print information command : Paper type flag
     */
    public static final byte PI_KIND = (byte) 0x02;

    /**
     * Print information command : Media width flag
     */
    public static final byte PI_WIDTH = (byte) 0x04;

    /**
     * Print information command : Media length flag
     */
    public static final byte PI_LENGTH = (byte) 0x08;

    /**
     * Print information command : Priority given to print quality flag
     */
    public static final byte PI_QUALITY = (byte) 0x40;

    /**
     * Print information command : Printer recovery always on flag
     */
    public static final byte PI_RECOVER = (byte) 0x80;

    /**
     * Print information command : Starting page constant
     */
    public static final byte STARTING_PAGE = 0;

    private QL() {
        // Prevent instanciation
    }
}
