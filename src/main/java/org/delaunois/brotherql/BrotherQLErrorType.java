/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import lombok.Getter;
import org.delaunois.brotherql.util.Rx;

import java.util.EnumSet;

/**
 * Brother QL error types
 *
 * @author Cedric de Launois
 */
public enum BrotherQLErrorType {

    /**
     * No Media
     */
    NO_MEDIA_WHEN_PRINTING(1, 0, Rx.msg("errortype.nomedia")),

    /**
     * End of media
     */
    END_OF_MEDIA(1, 1, Rx.msg("errortype.endofmedia")),

    /**
     * Tape cutter jam
     */
    TAPE_CUTTER_JAM(1, 2, Rx.msg("errortype.tapecutterjam")),

    /**
     * Printer already in use
     */
    UNIT_IN_USE(1, 4, Rx.msg("errortype.unitinuse")),

    /**
     * Fan doesn't work
     */
    FAN_DOESNT_WORK(1, 7, Rx.msg("errortype.fan")),

    /**
     * I/O error
     */
    TRANSMISSION_ERROR(2, 2, Rx.msg("errortype.transmission")),

    /**
     * Cover is opened
     */
    COVER_OPENED(2, 4, Rx.msg("errortype.cover")),

    /**
     * Feeding issue
     */
    CANNOT_FEED(2, 6, Rx.msg("errortype.feed")),

    /**
     * Printer internal error
     */
    SYSTEM_ERROR(2, 7, Rx.msg("errortype.system"));

    /**
     * A user-frienldy message associated to the error
     */
    @Getter
    public final String message;

    /**
     * The raw error flags
     */
    public final int flag;

    BrotherQLErrorType(int errorId, int id, String message) {
        this.flag = 1 << id << ((2 - errorId) * 8);
        this.message = message;
    }

    /**
     * Translates an error code into a Set of BrotherQLErrorType enums
     *
     * @param err1 the error information byte 1
     * @param err2 the error information byte 2
     * @return EnumSet representing a status
     */
    public static EnumSet<BrotherQLErrorType> getStatusFlags(int err1, int err2) {
        int statusValue = (err1 << 8) + err2;
        EnumSet<BrotherQLErrorType> statusFlags = EnumSet.noneOf(BrotherQLErrorType.class);
        for (BrotherQLErrorType error : BrotherQLErrorType.values()) {
            if ((error.flag & statusValue) == error.flag) {
                statusFlags.add(error);
            }
        }
        return statusFlags;
    }

}
