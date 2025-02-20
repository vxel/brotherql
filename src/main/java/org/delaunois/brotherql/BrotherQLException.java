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

    public BrotherQLException(String s) {
        super(s);
    }

    public BrotherQLException(String s, int libusberror) {
        super(s + Rx.msg("error.libusb") + libusberror);
    }
    
    public BrotherQLException(String message, Throwable cause) {
        super(message, cause);
    }

}
