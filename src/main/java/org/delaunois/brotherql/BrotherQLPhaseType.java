/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import java.util.HashMap;
import java.util.Map;

/**
 * Brother QL printer phases
 * 
 * @author Cedric de Launois
 */
public enum BrotherQLPhaseType {
    
    WAITING_TO_RECEIVE((byte) 0x00),
    PHASE_PRINTING((byte) 0x01),
    UNKNOWN((byte) 0xFF);

    private static final Map<Byte, BrotherQLPhaseType> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLPhaseType mt : BrotherQLPhaseType.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    public final byte code;

    BrotherQLPhaseType(byte code) {
        this.code = code;
    }

    public static BrotherQLPhaseType fromCode(byte code) {
        return CODE_MAP.get(code);
    }
    
}
