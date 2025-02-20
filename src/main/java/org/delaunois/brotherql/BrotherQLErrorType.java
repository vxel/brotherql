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

    NO_MEDIA_WHEN_PRINTING(1,0, Rx.msg("errortype.nomedia")),
    END_OF_MEDIA(1, 1, Rx.msg("errortype.endofmedia")),
    TAPE_CUTTER_JAM(1, 2, Rx.msg("errortype.tapecutterjam")),
    UNIT_IN_USE(1, 4, Rx.msg("errortype.unitinuse")),
    FAN_DOESNT_WORK(1, 7, Rx.msg("errortype.fan")),
    TRANSMISSION_ERROR(2,2, Rx.msg("errortype.transmission")),
    COVER_OPENED(2, 4, Rx.msg("errortype.cover")),
    CANNOT_FEED(2, 6, Rx.msg("errortype.feed")),
    SYSTEM_ERROR(2, 7, Rx.msg("errortype.system"));

    @Getter
    public final String message;

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
