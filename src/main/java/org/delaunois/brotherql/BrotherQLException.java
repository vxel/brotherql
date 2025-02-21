/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import org.delaunois.brotherql.util.Rx;

/**
 * A generic exception for error related to Brother QL printing.
 *
 * @author Cedric de Launois
 */
public class BrotherQLException extends Exception {

    /**
     * Construct the exception with the given error message.
     *
     * @param message the error message
     */
    public BrotherQLException(String message) {
        super(message);
    }

    /**
     * Construct the exception with the given error message and given libusb error id.
     *
     * @param message     the error message
     * @param libusberror the libusb error id
     */
    public BrotherQLException(String message, int libusberror) {
        super(message + Rx.msg("error.libusb") + libusberror);
    }

    /**
     * Construct the exception with the given error message and given cause.
     *
     * @param message the error message
     * @param cause   the exception cause
     */
    public BrotherQLException(String message, Throwable cause) {
        super(message, cause);
    }

}
