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
 * Brother QL printer models.
 *
 * @author Cedric de Launois
 */
public enum BrotherQLPrinterId {

    /**
     * Brother QL-500
     */
    QL_500("Brother QL-500", 0x2015, true, 295, 11811, true),

    /**
     * Brother QL-550
     */
    QL_550("Brother QL-550", 0x2016, false, 295, 11811, true),

    /**
     * Brother QL-560
     */
    QL_560("Brother QL-560", 0x2027, false, 295, 11811, true),

    /**
     * Brother QL-570
     */
    QL_570("Brother QL-570", 0x2028, false, 150, 11811, true),

    /**
     * Brother QL-580N
     */
    QL_580N("Brother QL-580N", 0x2029, false, 150, 11811, false),

    /**
     * Brother QL-650TD
     */
    QL_650TD("Brother QL-650TD", 0x201B, true, 295, 11811, false),

    /**
     * Brother QL-700
     */
    QL_700_P("Brother QL-700", 0x2042, false, 150, 11811, true),

    /**
     * Brother QL-700M
     */
    QL_700_M("Brother QL-700M", 0x2049, false, 150, 11811, true),

    /**
     * Brother QL-1050
     */
    QL_1050("Brother QL-1050", 0x2020, true, 295, 35433, false),

    /**
     * Brother QL-1060N
     */
    QL_1060N("Brother QL-1060N", 0x202A, true, 295, 35433, false),

    /**
     * Unknown printer
     */
    UNKNOWN(Rx.msg("printerid.unknown"), 0x000, false, 0, 0, true);

    private static final Map<Integer, BrotherQLPrinterId> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLPrinterId mt : BrotherQLPrinterId.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    /**
     * The name of the printer.
     */
    public final String name;

    /**
     * The code of the printer.
     */
    public final int code;

    /**
     * Whether the printer allows feed margin.
     */
    public final boolean allowsFeedMargin;

    /**
     * The minimum length/height in dots when printing on a continuous tape.
     */
    public final int clMinLengthPx;

    /**
     * The maximum length/height in dots when printing on a continuous tape.
     */
    public final int clMaxLengthPx;

    /**
     * Whether the printer supports only the raster protocol or not.
     */
    public final boolean rasterOnly;

    BrotherQLPrinterId(String name, Integer code, boolean allowsFeedMargin, int clMinLengthPx, int clMaxLengthPx, boolean rasterOnly) {
        this.code = code;
        this.allowsFeedMargin = allowsFeedMargin;
        this.clMinLengthPx = clMinLengthPx;
        this.clMaxLengthPx = clMaxLengthPx;
        this.name = name;
        this.rasterOnly = rasterOnly;
    }

    /**
     * Identify the printer based on the code.
     *
     * @param code the code
     * @return the printer Id
     */
    public static BrotherQLPrinterId fromCode(int code) {
        BrotherQLPrinterId printerId = CODE_MAP.get(code);
        return printerId == null ? UNKNOWN : printerId;
    }

    @Override
    public String toString() {
        return name;
    }
}
