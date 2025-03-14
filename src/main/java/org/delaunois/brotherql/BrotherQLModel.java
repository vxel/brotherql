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
public enum BrotherQLModel {

    /**
     * Brother QL-500
     */
    QL_500("QL-500", 0x2015, true, 295, 11811, true, false),

    /**
     * Brother QL-550
     */
    QL_550("QL-550", 0x2016, false, 295, 11811, true, false),

    /**
     * Brother QL-560
     */
    QL_560("QL-560", 0x2027, false, 295, 11811, true, false),

    /**
     * Brother QL-570
     */
    QL_570("QL-570", 0x2028, false, 150, 11811, true, true),

    /**
     * Brother QL-580N
     */
    QL_580N("QL-580N", 0x2029, false, 150, 11811, false, true),

    /**
     * Brother QL-650TD
     */
    QL_650TD("QL-650TD", 0x201B, true, 295, 11811, false, false),

    /**
     * Brother QL-700
     */
    QL_700_P("QL-700", 0x2042, false, 150, 11811, true, true),

    /**
     * Brother QL-700M
     */
    QL_700_M("QL-700M", 0x2049, false, 150, 11811, true, true),

    /**
     * Brother QL-1050
     */
    QL_1050("QL-1050", 0x2020, true, 295, 35433, false, false),

    /**
     * Brother QL-1060N
     */
    QL_1060N("QL-1060N", 0x202A, true, 295, 35433, false, false),

    /**
     * Unknown printer
     */
    UNKNOWN(Rx.msg("model.unknown"), 0x000, false, 0, 0, true, false);

    private static final Map<Integer, BrotherQLModel> CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLModel mt : BrotherQLModel.values()) {
            CODE_MAP.put(mt.code, mt);
        }
    }

    /**
     * The model of the printer.
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

    /**
     * Whether the printer support 600 height dpi x 300 wide dpi resolution or not.
     */
    public final boolean dpi600;

    BrotherQLModel(String name, Integer code, boolean allowsFeedMargin, int clMinLengthPx, int clMaxLengthPx, boolean rasterOnly, boolean dpi600) {
        this.code = code;
        this.allowsFeedMargin = allowsFeedMargin;
        this.clMinLengthPx = clMinLengthPx;
        this.clMaxLengthPx = clMaxLengthPx;
        this.name = name;
        this.rasterOnly = rasterOnly;
        this.dpi600 = dpi600;
    }

    /**
     * Identify the printer based on the code.
     *
     * @param code the code
     * @return the printer model
     */
    public static BrotherQLModel fromCode(int code) {
        BrotherQLModel model = CODE_MAP.get(code);
        return model == null ? UNKNOWN : model;
    }

    @Override
    public String toString() {
        return name;
    }
}
