/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql.backend;

import lombok.Getter;
import org.delaunois.brotherql.BrotherQLException;
import org.delaunois.brotherql.BrotherQLModel;
import org.delaunois.brotherql.util.Hex;
import org.delaunois.brotherql.util.Rx;
import org.usb4java.BufferUtils;
import org.usb4java.ConfigDescriptor;
import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.DeviceList;
import org.usb4java.EndpointDescriptor;
import org.usb4java.Interface;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.usb4java.LibUsb.ENDPOINT_DIR_MASK;
import static org.usb4java.LibUsb.ENDPOINT_IN;
import static org.usb4java.LibUsb.ENDPOINT_OUT;

/**
 * Access layer to USB Brother QL device printers.
 *
 * @author Cedric de Launois
 */
public class BrotherQLDeviceUsb implements BrotherQLDevice {

    private static final Logger LOGGER = System.getLogger(BrotherQLDeviceUsb.class.getName());

    /**
     * The vendor ID of the Brother QL Printer.
     */
    private static final short BROTHER_VENDOR_ID = 0x04f9;
    private static final int STATUS_SIZE = 32;

    @Getter
    private BrotherQLModel model;

    private final URI uri;
    private Context context;
    private DeviceHandle handle;
    private DeviceDescriptor deviceDescriptor;
    private EndpointDescriptor epIn;
    private EndpointDescriptor epOut;

    /**
     * Construct a backend for the first USB Brother printer found.
     */
    public BrotherQLDeviceUsb() {
        this(null);
    }

    /**
     * Construct the backend for the USB Brother printer identified by the given URI.
     *
     * @param uri the URI identifier for the USB printer : a string like usb://Brother/QL-700?serial=XXX
     */
    public BrotherQLDeviceUsb(URI uri) {
        this.uri = uri;
        this.handle = null;
        this.context = new Context();

        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException(Rx.msg("libusb.initerror"), result);
        LibUsb.setOption(context, LibUsb.OPTION_LOG_LEVEL, LibUsb.LOG_LEVEL_INFO);

        if (!"usb".equals(uri.getScheme())) {
            throw new IllegalArgumentException("Only usb supported");
        }

        if (!"Brother".equals(uri.getHost())) {
            throw new IllegalArgumentException("Only Brother printers supported");
        }
    }

    @Override
    public void open() throws BrotherQLException {
        if (handle != null) {
            throw new IllegalStateException(Rx.msg("libusb.alreadyopened"));
        }

        // Find the requested Brother device
        Device device = findDevice(uri);
        if (device == null) {
            throw new BrotherQLException(Rx.msg("libusb.nodevicelist"));
        }

        int result = LibUsb.setAutoDetachKernelDriver(handle, true);
        if (result != LibUsb.SUCCESS) {
            LOGGER.log(Level.WARNING, "setAutoDetachKernelDriver failed: " + result);
        }

        // Get the USB configuration
        LibUsb.setConfiguration(handle, 1);
        IntBuffer intBuffer = IntBuffer.allocate(1);
        result = LibUsb.getConfiguration(handle, intBuffer);
        switch (result) {
            case LibUsb.SUCCESS:
                break;
            case LibUsb.ERROR_NO_DEVICE:
                LibUsb.close(handle);
                throw new BrotherQLException(Rx.msg("libusb.nodevice"));
            default:
                LibUsb.close(handle);
                throw new BrotherQLException(Rx.msg("libusb.noconfig"));
        }

        // Get the USB interface
        InterfaceDescriptor descriptor = findDescriptor(device, (byte) intBuffer.get(0), ifd -> ifd.bInterfaceClass() == LibUsb.CLASS_PRINTER);

        // Find the IN and OUT endpoints
        epIn = findEndpoint(descriptor, ep -> (ep.bEndpointAddress() & ENDPOINT_DIR_MASK) == ENDPOINT_IN);
        epOut = findEndpoint(descriptor, ep -> (ep.bEndpointAddress() & ENDPOINT_DIR_MASK) == ENDPOINT_OUT);

        // Claim the interface
        result = LibUsb.claimInterface(handle, descriptor.bInterfaceNumber());
        switch (result) {
            case LibUsb.SUCCESS:
                break;
            case LibUsb.ERROR_NOT_FOUND:
                LibUsb.close(handle);
                throw new BrotherQLException(Rx.msg("libusb.notfound"));
            case LibUsb.ERROR_BUSY:
                LibUsb.close(handle);
                throw new BrotherQLException(Rx.msg("libusb.busy"));
            case LibUsb.ERROR_NO_DEVICE:
                LibUsb.close(handle);
                throw new BrotherQLException(Rx.msg("libusb.nodevice"));
            default:
                LibUsb.close(handle);
                throw new BrotherQLException(Rx.msg("libusb.noclaim"));
        }
    }

    /**
     * List the detected Brother USB devices.
     *
     * @return the string identifier of the detected printers
     * @throws BrotherQLException if a libusb error occurred while getting the device list
     */
    public static List<String> listDevices() throws BrotherQLException {
        LibUsb.init(null);
        List<String> devices = new ArrayList<>();
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) {
            LibUsb.exit(null);
            throw new BrotherQLException(Rx.msg("libusb.nodevicelist"), result);
        }

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new BrotherQLException(Rx.msg("libusb.devicereadfailure"), result);
                }

                BrotherQLModel pId = BrotherQLModel.fromCode(descriptor.idProduct());
                if (descriptor.idVendor() == BROTHER_VENDOR_ID && pId != null) {
                    DeviceHandle deviceHandle = openDevice(device);
                    String deviceSerial = LibUsb.getStringDescriptor(deviceHandle, descriptor.iSerialNumber());
                    String uri = String.format("usb://Brother/%s?serial=%s", pId, deviceSerial);
                    devices.add(uri);
                    LibUsb.close(deviceHandle);
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
            LibUsb.exit(null);
        }

        // Device not found
        return devices;
    }

    @Override
    public boolean isClosed() {
        return handle == null;
    }

    private BrotherQLModel getModelFromUri(URI uri) {
        if (uri == null || uri.getPath() == null || !uri.getPath().startsWith("/")) {
            return null;
        }

        String path = uri.getPath().substring(1);
        for (BrotherQLModel m : BrotherQLModel.values()) {
            if (path.equals(m.name)) {
                return m;
            }
        }
        return null;
    }

    private String getSerialFromUri(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }

        String[] queryParts = uri.getQuery().split("=");
        if (queryParts.length < 2 || !queryParts[0].equals("serial")) {
            return null;
        }
        return queryParts[1];
    }

    /**
     * Find a Brother USB device that we know, based on BrotherQLModel enum.
     *
     * @return the device, if found, null otherwise
     * @throws BrotherQLException if a libusb error occurred while getting the device list
     */
    private Device findDevice(URI uri) throws BrotherQLException {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(context, list);
        if (result < 0) {
            throw new BrotherQLException(Rx.msg("libusb.nodevicelist"), result);
        }

        BrotherQLModel model = getModelFromUri(uri);
        String serial = getSerialFromUri(uri);

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new BrotherQLException(Rx.msg("libusb.devicereadfailure"), result);
                }

                BrotherQLModel pId = BrotherQLModel.fromCode(descriptor.idProduct());
                if (descriptor.idVendor() == BROTHER_VENDOR_ID && pId != null && (model == null || pId == model)) {
                    // A known Brother device was found !
                    DeviceHandle deviceHandle = openDevice(device);
                    String deviceSerial = LibUsb.getStringDescriptor(deviceHandle, descriptor.iSerialNumber());
                    if (serial == null || serial.equals(deviceSerial)) {
                        this.handle = deviceHandle;
                        this.deviceDescriptor = descriptor;
                        this.model = pId;
                        LOGGER.log(Level.DEBUG, "Found printer {0}", this.model);
                        return device;
                    } else {
                        // Wrong device, continue
                        LOGGER.log(Level.DEBUG, "Brother printer {0} s/n {1} not matching requested URI {2}",
                                pId, deviceSerial, uri);
                        LibUsb.close(deviceHandle);
                    }
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }

    private static DeviceHandle openDevice(Device device) throws BrotherQLException {
        // Open a connection to the device
        DeviceHandle deviceHandle = new DeviceHandle();
        int result = LibUsb.open(device, deviceHandle);
        switch (result) {
            case LibUsb.SUCCESS:
                break;
            case LibUsb.ERROR_NO_MEM:
                throw new BrotherQLException(Rx.msg("libusb.nomem"));
            case LibUsb.ERROR_ACCESS:
                throw new BrotherQLException(Rx.msg("libusb.noaccess"));
            case LibUsb.ERROR_NO_DEVICE:
                throw new BrotherQLException(Rx.msg("libusb.nodevice"));
            default:
                throw new BrotherQLException(Rx.msg("libusb.unknown"), result);
        }
        return deviceHandle;
    }

    /**
     * Find the interface that matches the given predicate.
     *
     * @param device the device, must be opened first
     * @param config the configuration id
     * @param filter the predicate
     * @return the found interface
     * @throws BrotherQLException if no interface matches the predicate
     */
    private InterfaceDescriptor findDescriptor(Device device, byte config, Predicate<InterfaceDescriptor> filter) throws BrotherQLException {
        ConfigDescriptor configDescriptor = new ConfigDescriptor();
        int result = LibUsb.getConfigDescriptorByValue(device, config, configDescriptor);
        if (result != LibUsb.SUCCESS) {
            throw new BrotherQLException(Rx.msg("libusb.ifnotfound"), result);
        }

        Interface[] ifaces = configDescriptor.iface();
        for (Interface iface : ifaces) {
            InterfaceDescriptor[] ifDescriptors = iface.altsetting();
            for (InterfaceDescriptor ifDescriptor : ifDescriptors) {
                if (filter.test(ifDescriptor)) {
                    return ifDescriptor;
                }
            }
        }
        throw new BrotherQLException(Rx.msg("libusb.ifnotfound"));
    }

    /**
     * Find the endpoint that matches the given predicate.
     *
     * @param iface  the interface descriptor
     * @param filter the predicate
     * @return the found endpoint
     * @throws BrotherQLException if no endpoint matches the predicate
     */
    private EndpointDescriptor findEndpoint(InterfaceDescriptor iface, Predicate<EndpointDescriptor> filter) throws BrotherQLException {
        for (EndpointDescriptor endpoint : iface.endpoint()) {
            if (filter.test(endpoint)) {
                return endpoint;
            }
        }
        throw new BrotherQLException(Rx.msg("libusb.epnotfound"));
    }

    @Override
    public void write(byte[] data, long timeout) throws BrotherQLException {
        LOGGER.log(Level.DEBUG, "Tx: " + Hex.toString(data));

        ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
        buffer.put(data);
        try {
            write(handle, epOut, buffer, timeout);
        } catch (IOException e) {
            throw new BrotherQLException(Rx.msg("error.senderror"), e);
        }
    }

    private static void write(DeviceHandle handle, EndpointDescriptor epOut, ByteBuffer buffer, long timeout) throws IOException {
        IntBuffer transferred = BufferUtils.allocateIntBuffer();

        int result = LibUsb.bulkTransfer(handle, epOut.bEndpointAddress(), buffer, transferred, timeout);
        if (result != LibUsb.SUCCESS) {
            throw new IOException(Rx.msg("error.senderror") + " (" + result + ")");
        }
    }

    @Override
    public ByteBuffer readStatus(long timeout) {
        ByteBuffer buffer = BufferUtils.allocateByteBuffer(STATUS_SIZE).order(ByteOrder.LITTLE_ENDIAN);

        int read = rawread(handle, epIn, buffer, timeout);
        if (read == 0) {
            try {
                Thread.sleep(20);
                read = rawread(handle, epIn, buffer, timeout);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

        if (read > 0 && read < STATUS_SIZE) {
            LOGGER.log(Level.WARNING, "Incomplete read : " + read + " < " + 32 + " bytes : " + Hex.toString(buffer));
            return null;
        }

        return buffer;
    }

    private static int rawread(DeviceHandle handle, EndpointDescriptor epIn, ByteBuffer buffer, long timeout) {
        IntBuffer transferred = BufferUtils.allocateIntBuffer();
        int result = LibUsb.bulkTransfer(handle, epIn.bEndpointAddress(), buffer, transferred, timeout);
        if (result != LibUsb.SUCCESS) {
            LOGGER.log(Level.WARNING, Rx.msg("error.readerror") + result);
        }

        int read = transferred.get();
        if (read > 0 && LOGGER.isLoggable(Level.DEBUG)) {
            LOGGER.log(Level.DEBUG, "Rx: " + Hex.toString(buffer));
        }
        return read;
    }

    @Override
    public void close() {
        if (handle != null && deviceDescriptor != null) {
            LibUsb.close(handle);
            handle = null;
        }
        LibUsb.exit(context);
        context = new Context();
    }

}
