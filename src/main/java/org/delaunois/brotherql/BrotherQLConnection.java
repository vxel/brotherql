/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import org.delaunois.brotherql.backend.BrotherQLDevice;
import org.delaunois.brotherql.backend.BrotherQLDeviceUsb;
import org.delaunois.brotherql.util.BitOutputStream;
import org.delaunois.brotherql.util.Converter;
import org.delaunois.brotherql.util.Rx;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Main class implementing the Brother QL printer raster protocol communication.
 *
 * @author Cedric de Launois
 */
public final class BrotherQLConnection {

    private static final Logger LOGGER = System.getLogger(BrotherQLConnection.class.getName());

    /**
     * The printer read timeout in milliseconds.
     */
    private static final int TIMEOUT = 1000;

    /**
     * The print timeout in milliseconds. When printing, we expect the printer gives a feedback before this timeout.
     */
    private static final int PRINT_TIMEOUT_MS = 2000;

    private static final int STATUS_SIZE = 32;

    private static final byte[] CMD_RESET = new byte[350];
    private static final byte[] CMD_INITIALIZE = new byte[]{0x1B, 0x40};
    private static final byte[] CMD_STATUS_REQUEST = new byte[]{0x1B, 0x69, 0x53};
    private static final byte[] CMD_PRINT_INFORMATION = new byte[]{0x1B, 0x69, 0x7A};
    private static final byte[] CMD_SET_AUTOCUT_ON = new byte[]{0x1B, 0x69, 0x4D, 0x40};
    private static final byte[] CMD_SET_AUTOCUT_OFF = new byte[]{0x1B, 0x69, 0x4D, 0};
    private static final byte[] CMD_SET_CUT_PAGENUMBER = new byte[]{0x1B, 0x69, 0x41};
    private static final byte[] CMD_SET_MARGIN = new byte[]{0x1B, 0x69, 0x64};
    private static final byte[] CMD_RASTER_GRAPHIC_TRANSFER = new byte[]{0x67, 0x00};
    private static final byte[] CMD_PRINT = new byte[]{0x0C};
    private static final byte[] CMD_PRINT_LAST = new byte[]{0x1A};
    private static final byte[] CMD_SWITCH_TO_RASTER = new byte[]{0x1B, 0x69, 0x61, 0x01};

    private static final byte PI_KIND = (byte) 0x02; // Paper type
    private static final byte PI_WIDTH = (byte) 0x04; // Paper width
    private static final byte PI_LENGTH = (byte) 0x08; // Paper length
    private static final byte PI_QUALITY = (byte) 0x40; // Give priority to print quality
    private static final byte PI_RECOVER = (byte) 0x80; // Always ON
    private static final byte STARTING_PAGE = 0;

    private BrotherQLDevice device;

    /**
     * Construct a connection assuming a USB Printer Device.
     */
    public BrotherQLConnection() {
        this.device = new BrotherQLDeviceUsb();
    }

    /**
     * Construct a connection assuming the given Printer Device.
     *
     * @param device the backend device to use
     */
    public BrotherQLConnection(BrotherQLDevice device) {
        this.device = device;
    }

    /**
     * Search for a USB Brother printer and open a connection.
     *
     * @throws IllegalStateException if the printer is already opened
     * @throws BrotherQLException    if the USB connection could not be established
     */
    public void open() throws BrotherQLException {
        device.open();
        // Initialize the printer 
        reset();
    }

    /**
     * Reset the printer state.
     *
     * @throws BrotherQLException if the reset instruction could not be sent
     */
    public void reset() throws BrotherQLException {
        device.write(CMD_RESET, TIMEOUT);
        device.write(CMD_INITIALIZE, TIMEOUT);
    }

    /**
     * Get the printer identification.
     * The device must be opened first.
     *
     * @return the printer id or null if no Brother printer were detected
     */
    public BrotherQLPrinterId getPrinterId() {
        return device.getPrinterId();
    }

    /**
     * Send to the printer a request for status and read back the status.
     * Must NOT be called while printing, otherwise the print will immediately stop.
     * The device must be opened first.
     *
     * @return the status
     * @throws BrotherQLException if the connection fails
     */
    public BrotherQLStatus requestDeviceStatus() throws BrotherQLException {
        if (device.isClosed()) {
            return new BrotherQLStatus(null, device.getPrinterId(), Rx.msg("error.notopened"));
        }

        device.write(CMD_STATUS_REQUEST, TIMEOUT);
        return readDeviceStatus();
    }

    /**
     * Read the status of the printer.
     * The device must be opened first.
     *
     * @return the status or null if no status were received
     */
    public BrotherQLStatus readDeviceStatus() {
        if (device.isClosed()) {
            return new BrotherQLStatus(null, device.getPrinterId(), Rx.msg("error.notopened"));
        }

        BrotherQLStatus brotherQLStatus;
        ByteBuffer response = device.readStatus(TIMEOUT);

        if (response == null) {
            return null;

        } else {
            byte[] status = new byte[STATUS_SIZE];
            response.get(status);
            brotherQLStatus = new BrotherQLStatus(status, device.getPrinterId());
        }

        LOGGER.log(Level.DEBUG, "Status is " + brotherQLStatus);
        return brotherQLStatus;
    }

    /**
     * Extract the media definition from the given printer status.
     *
     * @param status the printer status
     * @return the media definition
     */
    public BrotherQLMedia getMediaDefinition(BrotherQLStatus status) {
        return BrotherQLMedia.identify(status);
    }

    /**
     * Send the given Job for printing.
     *
     * @param job            the job to print.
     * @param statusListener a lambda called after each print. The lambda receives as argument the page
     *                       number that was printed (starting at 0) and the current status,
     *                       and must return a boolean telling whether the print must continue or not.
     * @throws BrotherQLException if the job is missing information, or if the printer is not ready,
     *                            or if another print error occurred
     */
    public void sendJob(BrotherQLJob job, BiFunction<Integer, BrotherQLStatus, Boolean> statusListener)
            throws BrotherQLException {
        if (job == null
                || job.getImages() == null
                || job.getImages().isEmpty()
                || device.isClosed()) {
            throw new BrotherQLException(Rx.msg("error.incompletejob"));
        }

        BrotherQLStatus status = requestDeviceStatus();
        if (status.getStatusType() != BrotherQLStatusType.READY) {
            throw new BrotherQLException(Rx.msg("error.notready"));
        }

        BrotherQLMedia media = getMediaDefinition(status);
        List<BufferedImage> images = raster(job);
        checkJob(images, media);
        sendControlCode(images, job, media);

        for (int i = 0; i < images.size(); i++) {

            sendPrintData(images.get(i), media);

            boolean last = i == images.size() - 1;
            byte[] pc = last ? CMD_PRINT_LAST : CMD_PRINT;

            device.write(pc, TIMEOUT);

            // Should read PHASE_CHANGE PHASE_PRINTING
            status = readDeviceStatus();
            int timeleft = PRINT_TIMEOUT_MS;
            while (timeleft > 0 && (status == null || BrotherQLPhaseType.PHASE_PRINTING.equals(status.getPhaseType()))) {
                timeleft -= 200;
                sleep(200);
                status = readDeviceStatus();
            }

            if (statusListener != null) {
                if (!statusListener.apply(i, status)) {
                    break;
                }
            }

            if (shouldStopPrint(status)) {
                break;
            }

            sleep(job.getDelay());
        }
    }

    /**
     * Convert the job images to monochrome according to the batch options
     * (dithering, brightness, threshold, rotation...).
     * This method can be used to preview the labels.
     *
     * @param job the job
     * @return the rastered images
     */
    public static List<BufferedImage> raster(BrotherQLJob job) {
        List<BufferedImage> images = job.getImages();
        List<BufferedImage> convertedImages = new ArrayList<>();

        for (BufferedImage image : images) {
            BufferedImage converted = image;
            if (job.getRotate() != 0) {
                converted = Converter.rotate(image, job.getRotate());
            }

            if (job.isDither()) {
                converted = Converter.floydSteinbergDithering(converted, job.getBrightness());
            } else {
                converted = Converter.threshold(converted, job.getThreshold());
            }
            convertedImages.add(converted);
        }

        return convertedImages;
    }

    /**
     * Close the printer connection.
     * Should be closed before your application exits.
     */
    public void close() {
        device.close();
    }

    private boolean shouldStopPrint(BrotherQLStatus status) {
        if (status == null) {
            LOGGER.log(Level.WARNING, "Could not get printer status within " + PRINT_TIMEOUT_MS + " ms. Stop printing.");
            return true;
        }

        if (BrotherQLPhaseType.PHASE_PRINTING.equals(status.getPhaseType())) {
            LOGGER.log(Level.WARNING, "Printer did not finish printing within " + PRINT_TIMEOUT_MS + " ms. Stop printing.");
            return true;
        }

        if (BrotherQLStatusType.ERROR_OCCURRED.equals(status.getStatusType())) {
            LOGGER.log(Level.WARNING, "Printer is in error. Stop printing.");
            return true;
        }

        if (!BrotherQLPhaseType.WAITING_TO_RECEIVE.equals(status.getPhaseType())) {
            LOGGER.log(Level.WARNING, "Printer is not ready to continue. Stop printing.");
            return true;
        }

        return false;
    }

    private void checkJob(List<BufferedImage> images, BrotherQLMedia media) throws BrotherQLException {
        BufferedImage img = images.get(0);
        int bodyLengthPx = img.getHeight();
        int bodyWidthPx = img.getWidth();
        LOGGER.log(Level.DEBUG, "Image size: " + bodyWidthPx + " x " + bodyLengthPx);
        LOGGER.log(Level.DEBUG, "Expected Image size: " + media.bodyWidthPx + " x " + media.bodyLengthPx);

        if (bodyWidthPx != media.bodyWidthPx) {
            throw new BrotherQLException(String.format(Rx.msg("error.img.badwidth"), media.bodyWidthPx));
        }

        BrotherQLPrinterId printerId = device.getPrinterId();
        if (BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE.equals(media.mediaType)) {
            if (bodyLengthPx < printerId.clMinLengthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.minheight"), printerId.clMinLengthPx));
            }
            if (bodyLengthPx > printerId.clMaxLengthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.maxheight"), printerId.clMaxLengthPx));
            }
        } else {
            if (bodyLengthPx != media.bodyLengthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.badheight"), media.bodyLengthPx));
            }
        }

        for (BufferedImage image : images) {
            if (image.getHeight() != bodyLengthPx || image.getWidth() != bodyWidthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.vary")));
            }
        }
    }

    private void sendControlCode(List<BufferedImage> images, BrotherQLJob job, BrotherQLMedia media) throws BrotherQLException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // Switch to raster if multiple modes exist
            if (!device.getPrinterId().rasterOnly) {
                bos.write(CMD_SWITCH_TO_RASTER);
            }

            // Add print info
            bos.write(CMD_PRINT_INFORMATION);
            byte pi = (byte) (PI_KIND | PI_WIDTH | PI_QUALITY | PI_RECOVER);
            if (!BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE.equals(media.mediaType)) {
                pi |= PI_LENGTH;
            }
            bos.write(pi);

            int bodyLengthPx = images.get(0).getHeight();
            bos.write(media.mediaType.code);
            bos.write(media.labelWidthMm & 0xFF);
            bos.write(media.labelLengthMm & 0xFF);
            bos.write(bodyLengthPx & 0xFF);
            bos.write((bodyLengthPx >> 8) & 0xFF);
            bos.write((bodyLengthPx >> 16) & 0xFF);
            bos.write((bodyLengthPx >> 24) & 0xFF);
            bos.write(STARTING_PAGE);
            bos.write(0);

            // Add autocut
            if (job.isAutocut()) {
                bos.write(CMD_SET_AUTOCUT_ON);
                bos.write(CMD_SET_CUT_PAGENUMBER);
                bos.write(job.getCutEach() & 0xFF);
            } else {
                bos.write(CMD_SET_AUTOCUT_OFF);
            }

            // Set margins (in dots)
            int feedAmount = getFeedAmount(job, media);
            bos.write(CMD_SET_MARGIN);
            bos.write(feedAmount & 0xFF);
            bos.write((feedAmount >> 8) & 0xFF);

            byte[] bytes = bos.toByteArray();
            device.write(bytes, TIMEOUT);
        } catch (IOException e) {
            throw new BrotherQLException(Rx.msg("error.senderror"), e);
        }
    }

    private int getFeedAmount(BrotherQLJob job, BrotherQLMedia media) {
        if (device.getPrinterId().allowsFeedMargin) {
            return job.getFeedAmount() & 0xFFFF;
        } else if (media.mediaType.equals(BrotherQLMediaType.DIE_CUT_LABEL)) {
            return 0;
        } else {
            return 35;
        }
    }

    private void sendPrintData(BufferedImage img, BrotherQLMedia media) throws BrotherQLException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BitOutputStream bitOutputStream = new BitOutputStream(bos);

            for (int y = 0; y < img.getHeight(); y++) {
                bos.reset();
                bos.write(CMD_RASTER_GRAPHIC_TRANSFER);
                bos.write(media.rgtSizeBytes);

                // Write left margin
                for (int i = 0; i < media.leftMarginPx; i++) {
                    bitOutputStream.write(0);
                }

                // Write body
                for (int x = media.bodyWidthPx - 1; x >= 0; x--) {
                    // Already monochrome B/W, just need to invert lsb
                    bitOutputStream.write(~img.getRGB(x, y) & 1);
                }

                // Write right margin
                for (int i = 0; i < media.leftMarginPx; i++) {
                    bitOutputStream.write(0);
                }

                bitOutputStream.close();
                byte[] bitRaster = bos.toByteArray();
                device.write(bitRaster, TIMEOUT);
            }
        } catch (IOException e) {
            throw new BrotherQLException(Rx.msg("error.senderror"), e);
        }
    }

    private void sleep(int millis) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

}
