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
 * Brother QL media types.
 *
 * @author Cedric de Launois
 */
public enum BrotherQLMediaType {

    /**
     * No media present.
     */
    NO_MEDIA(Rx.msg("mediatype.nomedia"), (byte) 0x00),

    /**
     * Laminated.
     */
    LAMINATED(Rx.msg("mediatype.laminated"), (byte) 0x01),

    /**
     * Non-laminated.
     */
    NON_LAMINATED(Rx.msg("mediatype.nonlaminated"), (byte) 0x03),

    /**
     * Fabric.
     */
    FABRIC_TAPE(Rx.msg("mediatype.fabric"), (byte) 0x04),

    /**
     * Continuous tape.
     */
    CONTINUOUS_LENGTH_TAPE(Rx.msg("mediatype.continuous"), (byte) 0x0A),

    /**
     * Die-cut label.
     */
    DIE_CUT_LABEL(Rx.msg("mediatype.diecut"), (byte) 0x0B),

    /**
     * Heat-Shrink Tube (HS 2:1).
     */
    HEAT_SHRINK_TUBE_21(Rx.msg("mediatype.heatshrinktube21"), (byte) 0x11),

    /**
     * Fle.
     */
    FLE(Rx.msg("mediatype.fle"), (byte) 0x13),

    /**
     * Flexible ID.
     */
    FLEXIBLE_ID(Rx.msg("mediatype.flexibleid"), (byte) 0x14),

    /**
     * Satin.
     */
    SATIN(Rx.msg("mediatype.satin"), (byte) 0x15),

    /**
     * Heat-Shrink Tube (HS 3:1).
     */
    HEAT_SHRINK_TUBE_31(Rx.msg("mediatype.heatshrinktube31"), (byte) 0x11),

    /**
     * Unknown type.
     */
    UNKNOWN(Rx.msg("mediatype.unknown"), (byte) 0xFF);

    private static final Map<Byte, BrotherQLMediaType> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLMediaType mt : BrotherQLMediaType.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    /**
     * User-friendly name of the media type.
     */
    public final String name;

    /**
     * Raw id code of the media type.
     */
    public final byte code;

    BrotherQLMediaType(String name, byte code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Identify the media type based on the code.
     *
     * @param code the code
     * @return the media type
     */
    public static BrotherQLMediaType fromCode(byte code) {
        return CODE_MAP.get(code);
    }

    /**
     * Get a user-friendly name of the media type.
     *
     * @return the name
     */
    public String toString() {
        return name;
    }
}
