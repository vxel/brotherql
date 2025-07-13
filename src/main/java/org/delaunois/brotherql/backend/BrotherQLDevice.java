/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql.backend;

import org.delaunois.brotherql.BrotherQLException;
import org.delaunois.brotherql.BrotherQLModel;

import java.nio.ByteBuffer;

/**
 * Backend interface for Brother QL printer communications.
 *
 * @author Cedric de Launois
 */
public interface BrotherQLDevice {

    /**
     * Search for a Brother printer and open a connection.
     *
     * @throws IllegalStateException if the printer is already opened
     * @throws BrotherQLException    if the connection could not be established (e.g. device not found,
     *                               not connected, busy)
     */
    void open() throws BrotherQLException;

    /**
     * Get the printer model.
     * The device must be opened first.
     * For network printers, the model is not known until a device status is received.
     *
     * @return the printer model or null if no Brother printer were detected
     */
    BrotherQLModel getModel();

    /**
     * Read a printer status.
     *
     * @param timeout timeout (in milliseconds) that this function should wait before giving up due to no response
     *                being received. For an unlimited timeout, use value 0.
     * @return the read data or null if did not receive a valid status within timeout
     */
    ByteBuffer readStatus(long timeout);

    /**
     * Writes some data to the printer.
     *
     * @param data    the data to send to the printer.
     * @param timeout timeout (in milliseconds) that this function should wait before giving up due to no
     *                response being received. For an unlimited timeout, use value 0.
     * @throws BrotherQLException if the data could not be sent
     */
    void write(byte[] data, long timeout) throws BrotherQLException;

    /**
     * Get whether the printer connection is closed or not.
     *
     * @return true if the printer connection is closed, false if it is open.
     */
    boolean isClosed();

    /**
     * Close the printer connection.
     * Should be closed before your application exits.
     */
    void close();

    /**
     * Tells whether the device is a USB device.
     * @return true if the device is a USB device, false otherwise
     */
    boolean isUsbPrinter();

}
