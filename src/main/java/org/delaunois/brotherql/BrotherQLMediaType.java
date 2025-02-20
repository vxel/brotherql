/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import org.delaunois.brotherql.util.Rx;

import java.util.HashMap;
import java.util.Map;

/**
 * Brother QL media types
 * 
 * @author Cedric de Launois
 */
public enum BrotherQLMediaType {
    
    NO_MEDIA(Rx.msg("mediatype.nomedia"), (byte) 0x00),
    CONTINUOUS_LENGTH_TAPE(Rx.msg("mediatype.continuous"), (byte) 0x0A),
    DIE_CUT_LABEL(Rx.msg("mediatype.diecut"), (byte) 0x0B),
    UNKNOWN(Rx.msg("mediatype.unknown"), (byte) 0xFF);

    private static final Map<Byte, BrotherQLMediaType> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLMediaType mt : BrotherQLMediaType.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    public final String name;
    public final byte code;

    BrotherQLMediaType(String name, byte code) {
        this.name = name;
        this.code = code;
    }

    public static BrotherQLMediaType fromCode(byte code) {
        return CODE_MAP.get(code);
    }

    public String toString() {
        return name;
    }
}
