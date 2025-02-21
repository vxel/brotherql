/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Objects;

/**
 * Hold and parse a Brother QL status
 *
 * @author Cedric de Launois
 */
public class BrotherQLStatus {

    private static final byte[] UNAVAILABLE = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, BrotherQLMediaType.UNKNOWN.code, 0, 0, 0, 0,
            0, 0, BrotherQLStatusType.PRINTER_UNAVAILABLE.code, BrotherQLPhaseType.UNKNOWN.code, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final byte[] NOT_CONNECTED = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, BrotherQLMediaType.UNKNOWN.code, 0, 0, 0, 0,
            0, 0, BrotherQLStatusType.PRINTER_NOT_CONNECTED.code, BrotherQLPhaseType.UNKNOWN.code, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    private byte[] status;

    /**
     * The printer id.
     */
    @Getter
    private BrotherQLPrinterId printerId;

    /**
     * A user-friendly exception message (if any).
     */
    @Getter
    private String exceptionMessage;

    /**
     * Construct a status with the given status bytes and the given printer id.
     *
     * @param status    the status
     * @param printerId the printer id
     */
    public BrotherQLStatus(byte[] status, BrotherQLPrinterId printerId) {
        this(status, printerId, null);
    }

    /**
     * Construct a status with the given status bytes, the given printer id and the given message.
     *
     * @param status           the status
     * @param printerId        the printer id
     * @param exceptionMessage the exception message
     */
    public BrotherQLStatus(byte[] status, BrotherQLPrinterId printerId, String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        this.printerId = Objects.requireNonNullElse(printerId, BrotherQLPrinterId.UNKNOWN);

        if (status == null && BrotherQLPrinterId.UNKNOWN.equals(this.printerId)) {
            this.status = NOT_CONNECTED;
        } else if (status == null) {
            this.status = UNAVAILABLE;
        } else if (status.length < 32) {
            this.status = UNAVAILABLE;
        } else {
            this.status = status;
        }
    }

    /**
     * Get the set of error types.
     * 
     * @return the error set
     */
    public EnumSet<BrotherQLErrorType> getErrors() {
        return BrotherQLErrorType.getStatusFlags(status[8], status[9]);
    }

    /**
     * Get media width, in mm.
     *
     * @return the width
     */
    public int getMediaWidth() {
        return status[10];
    }

    /**
     * Get media type.
     *
     * @return the type
     */
    public BrotherQLMediaType getMediaType() {
        return BrotherQLMediaType.fromCode(status[11]);
    }

    /**
     * Get media length, in mm. 0 for continuous tape.
     *
     * @return the width
     */
    public int getMediaLength() {
        return status[17];
    }

    /**
     * Get status type.
     *
     * @return the type
     */
    public BrotherQLStatusType getStatusType() {
        return BrotherQLStatusType.fromCode(status[18]);
    }

    /**
     * Get phase type.
     *
     * @return the type
     */
    public BrotherQLPhaseType getPhaseType() {
        return BrotherQLPhaseType.fromCode(status[19]);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("status=" + getStatusType()
                + " mediaType=" + getMediaType() + " (" + getDimension() + ")"
                + " phaseType=" + getPhaseType());

        EnumSet<BrotherQLErrorType> errors = getErrors();
        if (!errors.isEmpty()) {
            str.append(" errors=");
            for (BrotherQLErrorType error : errors) {
                str.append(error).append(" ");
            }
        }

        return str.toString();
    }

    private String getDimension() {
        int length = getMediaLength();
        int width = getMediaWidth();

        if (length == 0) {
            return width + "mm";
        } else {
            return width + "mm x " + length + "mm";
        }
    }
}
