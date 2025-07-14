/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import org.delaunois.brotherql.backend.BrotherQLDevice;
import org.delaunois.brotherql.backend.BrotherQLDeviceTcp;
import org.delaunois.brotherql.backend.BrotherQLDeviceUsb;
import org.delaunois.brotherql.util.BitOutputStream;
import org.delaunois.brotherql.util.Converter;
import org.delaunois.brotherql.util.Rx;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static org.delaunois.brotherql.protocol.QL.CMD_INITIALIZE;
import static org.delaunois.brotherql.protocol.QL.CMD_PRINT;
import static org.delaunois.brotherql.protocol.QL.CMD_PRINT_INFORMATION;
import static org.delaunois.brotherql.protocol.QL.CMD_PRINT_LAST;
import static org.delaunois.brotherql.protocol.QL.CMD_RASTER_GRAPHIC_TRANSFER;
import static org.delaunois.brotherql.protocol.QL.CMD_RESET;
import static org.delaunois.brotherql.protocol.QL.CMD_SET_AUTOCUT_OFF;
import static org.delaunois.brotherql.protocol.QL.CMD_SET_AUTOCUT_ON;
import static org.delaunois.brotherql.protocol.QL.CMD_SET_CUT_PAGENUMBER;
import static org.delaunois.brotherql.protocol.QL.CMD_SET_EXPANDED_MODE;
import static org.delaunois.brotherql.protocol.QL.CMD_SET_MARGIN;
import static org.delaunois.brotherql.protocol.QL.CMD_STATUS_REQUEST;
import static org.delaunois.brotherql.protocol.QL.CMD_SWITCH_TO_RASTER;
import static org.delaunois.brotherql.protocol.QL.CMD_TWO_COLOR_RASTER_GRAPHIC_TRANSFER_FIRST;
import static org.delaunois.brotherql.protocol.QL.CMD_TWO_COLOR_RASTER_GRAPHIC_TRANSFER_SECOND;
import static org.delaunois.brotherql.protocol.QL.PI_KIND;
import static org.delaunois.brotherql.protocol.QL.PI_LENGTH;
import static org.delaunois.brotherql.protocol.QL.PI_QUALITY;
import static org.delaunois.brotherql.protocol.QL.PI_RECOVER;
import static org.delaunois.brotherql.protocol.QL.PI_WIDTH;
import static org.delaunois.brotherql.protocol.QL.STARTING_PAGE;
import static org.delaunois.brotherql.protocol.QL.STATUS_SIZE;

/**
 * Main class implementing the Brother QL printer raster protocol communication.
 *
 * @author Cedric de Launois
 */
public final class BrotherQLConnection implements Closeable {

    private static final Logger LOGGER = System.getLogger(BrotherQLConnection.class.getName());

    /**
     * The printer read timeout in milliseconds.
     */
    private static final int TIMEOUT = 1000;

    /**
     * The print timeout in milliseconds. When printing, we expect the printer gives a feedback before this timeout.
     */
    private static final int PRINT_TIMEOUT_MS = 2000;


    private BrotherQLDevice device;

    /**
     * Construct a connection to the first USB Brother Printer found.
     */
    public BrotherQLConnection() {
        this.device = new BrotherQLDeviceUsb();
    }

    /**
     * Construct a connection to the device identified by the given printer identifier.
     * The identifier is a string like "usb://Brother/QL-700?serial=XXX" for USB printer, and
     * "tcp://host:port/QL-720NW" for network printers.
     * See {@link #listDevices()} to get the identifiers of USB connected printers.
     * If address is null, the first USB printer found is used.
     *
     * @param address the device address
     */
    public BrotherQLConnection(String address) {
        if (address == null || address.isEmpty()) {
            this.device = new BrotherQLDeviceUsb();

        } else {
            URI uri = URI.create(address);
            if ("usb".equals(uri.getScheme())) {
                this.device = new BrotherQLDeviceUsb(uri);
            } else if ("tcp".equals(uri.getScheme())) {
                this.device = new BrotherQLDeviceTcp(uri);
            } else {
                throw new UnsupportedOperationException("Scheme " + uri.getScheme() + " not supported");
            }
        }
    }

    /**
     * Construct a connection using the given Printer Device.
     *
     * @param device the backend device to use
     */
    public BrotherQLConnection(BrotherQLDevice device) {
        this.device = device;
    }

    /**
     * Get the list of connected Brother printers.
     * Only USB devices can be discovered.
     *
     * @return the list of printer identifiers, e.g. "usb://Brother/QL-700?serial=XXX"
     * @throws BrotherQLException if an error occurred while getting the device list
     */
    public static List<String> listDevices() throws BrotherQLException {
        // Only USB devices can be discovered
        return BrotherQLDeviceUsb.listDevices();
    }

    /**
     * Open a connection to the printer.
     *
     * @throws IllegalStateException if the printer is already opened
     * @throws BrotherQLException    if the connection could not be established
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
        // Switch to raster if multiple modes exist
        device.write(CMD_SWITCH_TO_RASTER, TIMEOUT);
        device.write(CMD_RESET, TIMEOUT);
        device.write(CMD_INITIALIZE, TIMEOUT);
    }

    /**
     * Get the printer model.
     * The device must be opened first.
     *
     * @return the printer model or null if no Brother printer were detected
     */
    public BrotherQLModel getModel() {
        return device.getModel();
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
            return new BrotherQLStatus(null, device.getModel(), Rx.msg("error.notopened"));
        }

        device.write(CMD_STATUS_REQUEST, TIMEOUT);
        BrotherQLStatus status = readDeviceStatus();
        if (status == null) {
            return new BrotherQLStatus(null, device.getModel(), Rx.msg("error.readerror"));
        }
        
        return status;
    }

    /**
     * Read the status of the printer.
     * The device must be opened first.
     *
     * @return the status or null if no status were received
     */
    public BrotherQLStatus readDeviceStatus() {
        if (device.isClosed()) {
            return new BrotherQLStatus(null, device.getModel(), Rx.msg("error.notopened"));
        }

        BrotherQLStatus brotherQLStatus;
        ByteBuffer response = device.readStatus(TIMEOUT);

        if (response == null) {
            return null;

        } else {
            byte[] status = new byte[STATUS_SIZE];
            response.get(status);
            brotherQLStatus = new BrotherQLStatus(status, device.getModel());
        }

        LOGGER.log(Level.DEBUG, "Status is " + brotherQLStatus);
        return brotherQLStatus;
    }

    /**
     * Extract the media definition from the given printer status.
     * Only for USB printers.
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
     * @param job the job to print.
     * @throws BrotherQLException if the job is missing information, or if the printer is not ready,
     *                            or if another print error occurred
     */
    public void sendJob(BrotherQLJob job) throws BrotherQLException {
        sendJob(job, null);
    }

    /**
     * Send the given Job for printing.
     *
     * @param job            the job to print.
     * @param statusListener a lambda called after each print (or null). The lambda receives as argument the page
     *                       number that was printed (starting at 0) and the current status,
     *                       and must return a boolean telling whether the print must continue or not.
     * @throws BrotherQLException if the job is missing information, or if the printer is not ready,
     *                            or if another print error occurred
     */
    public void sendJob(BrotherQLJob job, BiFunction<Integer, BrotherQLStatus, Boolean> statusListener)
            throws BrotherQLException {
        
        if (device.isClosed()
                || device.getModel() == null
                || device.getModel().equals(BrotherQLModel.UNKNOWN)) {
            throw new BrotherQLException(Rx.msg("statustype.notconnected"));
        }
        
        if (job == null
                || job.getImages() == null
                || job.getImages().isEmpty()) {
            throw new BrotherQLException(Rx.msg("error.incompletejob"));
        }

        device.write(CMD_SWITCH_TO_RASTER, TIMEOUT);

        BrotherQLStatus status = requestDeviceStatus();
        if (status.getStatusType() != BrotherQLStatusType.READY) {
            throw new BrotherQLException(Rx.msg("error.notready"));
        }

        BrotherQLMedia media = device.isUsbPrinter() ? getMediaDefinition(status) : job.getMedia();
        if (media == null) {
            throw new BrotherQLException(Rx.msg("mediatype.unknown"));
        }
        
        boolean twoColor = media.twoColor && device.getModel().twoColor;
        List<BufferedImage> images = raster(job);
        checkJob(job, images, media);
        sendControlCode(images, job, media);

        for (int i = 0; i < images.size(); i++) {

            sendPrintData(images.get(i), media, twoColor);

            boolean last = i == images.size() - 1;
            byte[] pc = last ? CMD_PRINT_LAST : CMD_PRINT;

            device.write(pc, TIMEOUT);

            if (device.isUsbPrinter()) {
                // For USB printers, a status is returned when the device starts printing
                // Should read PHASE_CHANGE PHASE_PRINTING
                status = readDeviceStatus();
                int timeleft = PRINT_TIMEOUT_MS;
                while (timeleft > 0 && (status == null || BrotherQLPhaseType.PHASE_PRINTING.equals(status.getPhaseType()))) {
                    timeleft -= 200;
                    sleep(200);
                    status = readDeviceStatus();
                }
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
     * If dpi600 option is set, the image width will be divided by 2.
     *
     * @param job the job
     * @return the rastered images
     */
    public static List<BufferedImage> raster(BrotherQLJob job) {
        List<BufferedImage> images = job.getImages();
        List<BufferedImage> convertedImages = new ArrayList<>();

        for (BufferedImage image : images) {
            BufferedImage converted = image;

            int bodyLengthPx = image.getHeight();
            int bodyWidthPx = image.getWidth();

            if (job.isDpi600()) {
                // High DPI : divide width by 2 (300dpi) but preserve height (600 dpi)
                converted = Converter.scale(converted, bodyWidthPx / 2, bodyLengthPx);
            }

            if (job.getRotate() != 0) {
                converted = Converter.rotate(converted, job.getRotate());
            }

            if (job.isDither()) {
                if (job.getMedia() != null && job.getMedia().twoColor) {
                    BufferedImage redLayer = Converter.extractColorLayer(converted, pixel -> pixel == 0xFFFF0000);
                    BufferedImage blackLayer = Converter.floydSteinbergDithering(converted, job.getBrightness());
                    converted = Converter.override(blackLayer, redLayer);
                } else {
                    converted = Converter.floydSteinbergDithering(converted, job.getBrightness());
                }
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

    private void checkJob(BrotherQLJob job, List<BufferedImage> images, BrotherQLMedia media) throws BrotherQLException {
        BufferedImage img = images.get(0);
        int bodyLengthPx = img.getHeight();
        int bodyWidthPx = img.getWidth();
        int expectedBodyLengthPx = job.isDpi600() ? media.bodyLengthPx * 2 : media.bodyLengthPx;
        int expectedBodyWidthPx = media.bodyWidthPx;
        LOGGER.log(Level.DEBUG, "Image size: " + bodyWidthPx + " x " + bodyLengthPx);
        LOGGER.log(Level.DEBUG, "Expected Image size: " + expectedBodyWidthPx + " x " + expectedBodyLengthPx);

        BrotherQLModel model = device.getModel();

        if (job.isDpi600() && !model.dpi600) {
            throw new BrotherQLException(String.format(Rx.msg("error.dpi600.unsupported"), model.name));
        }

        if (bodyWidthPx != expectedBodyWidthPx) {
            throw new BrotherQLException(String.format(Rx.msg("error.img.badwidth"), expectedBodyWidthPx));
        }

        if (BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE.equals(media.mediaType)) {
            if (bodyLengthPx < model.clMinLengthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.minheight"), model.clMinLengthPx));
            }
            if (bodyLengthPx > model.clMaxLengthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.maxheight"), model.clMaxLengthPx));
            }
        } else {
            if (bodyLengthPx != expectedBodyLengthPx) {
                throw new BrotherQLException(String.format(Rx.msg("error.img.badheight"), expectedBodyLengthPx));
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
            
            // Add print info
            bos.write(CMD_PRINT_INFORMATION);
            byte pi = (byte) (PI_KIND | PI_WIDTH | PI_QUALITY | PI_RECOVER);
            if (!BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE.equals(media.mediaType)) {
                pi |= PI_LENGTH;
            }
            bos.write(pi); // {n1}

            int bodyLengthPx = images.get(0).getHeight();
            bos.write(media.mediaType.code); // {n2}
            bos.write(media.labelWidthMm & 0xFF); // {n3}
            bos.write(media.labelLengthMm & 0xFF); // {n4}
            bos.write(bodyLengthPx & 0xFF); // {n5}
            bos.write((bodyLengthPx >> 8) & 0xFF); // {n6}
            bos.write((bodyLengthPx >> 16) & 0xFF); // {n7}
            bos.write((bodyLengthPx >> 24) & 0xFF); // {n8}
            bos.write(STARTING_PAGE); // {n9}
            bos.write(0); // {n10}

            // Add autocut
            if (job.isAutocut()) {
                bos.write(CMD_SET_AUTOCUT_ON);
                bos.write(CMD_SET_CUT_PAGENUMBER);
                bos.write(job.getCutEach() & 0xFF);
            } else {
                bos.write(CMD_SET_AUTOCUT_OFF);
            }

            // Set expanded mode / high resolution printing
            if (job.isDpi600()) {
                bos.write(CMD_SET_EXPANDED_MODE);
                bos.write(1 << 6);
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
        if (device.getModel().allowsFeedMargin) {
            return job.getFeedAmount() & 0xFFFF;
        } else if (media.mediaType.equals(BrotherQLMediaType.DIE_CUT_LABEL)) {
            return 0;
        } else {
            return 35;
        }
    }

    private void sendPrintData(BufferedImage img, BrotherQLMedia media, boolean twoColor) throws BrotherQLException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BitOutputStream bitOutputStream = new BitOutputStream(bos);

            for (int y = 0; y < img.getHeight(); y++) {
                bos.reset();
                
                if (twoColor) {
                    bos.write(CMD_TWO_COLOR_RASTER_GRAPHIC_TRANSFER_FIRST);
                    bos.write(media.rgtSizeBytes);
                    writeColorData(bitOutputStream, media, img, y, 0x0);
                    
                    bos.write(CMD_TWO_COLOR_RASTER_GRAPHIC_TRANSFER_SECOND);
                    bos.write(media.rgtSizeBytes);
                    writeColorData(bitOutputStream, media, img, y, 0xFF0000);

                } else {
                    bos.write(CMD_RASTER_GRAPHIC_TRANSFER);
                    bos.write(media.rgtSizeBytes);
                    writeColorData(bitOutputStream, media, img, y, 0x0);
                }
                
                bitOutputStream.close();
                byte[] bitRaster = bos.toByteArray();
                device.write(bitRaster, TIMEOUT);
            }
        } catch (IOException e) {
            throw new BrotherQLException(Rx.msg("error.senderror"), e);
        }
    }

    private void writeColorData(BitOutputStream bitOutputStream, BrotherQLMedia media, BufferedImage img, int rasterLine, int color) throws IOException {
        writeMargin(bitOutputStream, media.leftMarginPx);
        writeBody(bitOutputStream, media, img, rasterLine, color);
        writeMargin(bitOutputStream, media.rightMarginPx);
    }

    private static void writeBody(BitOutputStream bitOutputStream, BrotherQLMedia media, BufferedImage img, int rasterLine, int color) throws IOException {
        for (int x = media.bodyWidthPx - 1; x >= 0; x--) {
            // 0 means do not print the dot, 1 means print the dot, so negate the masked color
            int rgb = img.getRGB(x, rasterLine) & 0xFFFFFF;
            bitOutputStream.write((rgb == color) ? 1 : 0);
        }
    }

    private void writeMargin(BitOutputStream bitOutputStream, int numBits) throws IOException {
        for (int i = 0; i < numBits; i++) {
            bitOutputStream.write(0);
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
