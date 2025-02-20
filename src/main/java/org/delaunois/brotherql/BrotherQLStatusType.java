/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import lombok.Getter;
import org.delaunois.brotherql.util.Rx;

import java.util.HashMap;
import java.util.Map;

/**
 * Brother QL status types
 * 
 * @author Cedric de Launois
 */
@SuppressWarnings("unused")
public enum BrotherQLStatusType {
    
    READY((byte) 0x00, Rx.msg("statustype.ready")),
    PRINTING_COMPLETED((byte) 0x01, Rx.msg("statustype.completed")),
    ERROR_OCCURRED((byte) 0x02, Rx.msg("statustype.error")),
    NOTIFICATION((byte) 0x05, Rx.msg("statustype.notification")),
    PHASE_CHANGE((byte) 0x06, Rx.msg("statustype.phasechange")),

    // Custom status
    PRINTER_UNAVAILABLE((byte)0xF0, Rx.msg("statustype.unavailable")),
    PRINTER_NOT_CONNECTED((byte)0xF1, Rx.msg("statustype.notconnected"));
    
    private static final Map<Byte, BrotherQLStatusType> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLStatusType mt : BrotherQLStatusType.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    @Getter
    public final String message;

    public final byte code;

    BrotherQLStatusType(byte code, String message) {
        this.code = code;
        this.message = message;
    }

    public static BrotherQLStatusType fromCode(byte code) {
        return CODE_MAP.get(code);
    }

}
