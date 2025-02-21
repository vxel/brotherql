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

    /**
     * Waiting to receive phase.
     */
    WAITING_TO_RECEIVE((byte) 0x00),

    /**
     * Printing phase.
     */
    PHASE_PRINTING((byte) 0x01),

    /**
     * Unknown phase.
     */
    UNKNOWN((byte) 0xFF);

    private static final Map<Byte, BrotherQLPhaseType> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLPhaseType mt : BrotherQLPhaseType.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    /**
     * The raw code of the phase.
     */
    public final byte code;

    BrotherQLPhaseType(byte code) {
        this.code = code;
    }

    /**
     * Identify the phase based on the given code.
     *
     * @param code the code
     * @return the phase
     */
    public static BrotherQLPhaseType fromCode(byte code) {
        return CODE_MAP.get(code);
    }

}
