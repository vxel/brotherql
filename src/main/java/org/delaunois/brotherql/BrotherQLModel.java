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
    QL_500("QL-500", 0x2015, 0x4F, true, 295, 11811, true, false),

    /**
     * Brother QL-550
     */
    QL_550("QL-550", 0x2016, 0x4F, false, 295, 11811, true, false),

    /**
     * Brother QL-560
     */
    QL_560("QL-560", 0x2027, 0x31, false, 295, 11811, true, false),

    /**
     * Brother QL-570
     */
    QL_570("QL-570", 0x2028, 0x32, false, 150, 11811, true, true),

    /**
     * Brother QL-580N
     */
    QL_580N("QL-580N", 0x2029, 0x33, false, 150, 11811, false, true),

    /**
     * Brother QL-600
     */
    QL_600("QL-600", 0x20C0, 0x47, true, 150, 11811, false, true),

    /**
     * Brother QL-650TD
     */
    QL_650TD("QL-650TD", 0x201B, 0x51, true, 295, 11811, false, false),

    /**
     * Brother QL-700
     */
    QL_700_P("QL-700", 0x2042, 0x35, false, 150, 11811, true, true),

    /**
     * Brother QL-700M
     */
    QL_700_M("QL-700M", 0x2049, 0x35, false, 150, 11811, true, true),

    /**
     * Brother QL-710W
     */
    QL_710_W("QL-710W", 0x2043, 0x36, false, 150, 11811, true, true),

    /**
     * Brother QL-720NW
     */
    QL_720_NW("QL-720NW", 0x2044, 0x37, false, 150, 11811, true, true),

    /**
     * Brother QL-800
     */
    QL_800("QL-800", 0x209b, 0x38, false, 150, 11811, true, true),

    /**
     * Brother QL-810W
     */
    QL_810W("QL-810W", 0x209c, 0x39, false, 150, 11811, true, true),

    /**
     * Brother QL-820NWB
     */
    QL_810NWB("QL-820NWB", 0x209d, 0x41, false, 150, 11811, true, true),

    /**
     * Brother QL-1050
     */
    QL_1050("QL-1050", 0x2020, 0x50, true, 295, 35433, false, false),

    /**
     * Brother QL-1060N
     */
    QL_1060N("QL-1060N", 0x202A, 0x34, true, 295, 35433, false, false),

    /**
     * Brother QL-1100
     */
    QL_1100("QL-1100", 0x20a7, 0x43, false, 150, 35433, true, false),

    /**
     * Brother QL-1110NWB
     */
    QL_1110NWB("QL-1110NWB", 0x20a8, 0x44, false, 150, 35433, true, false),

    /**
     * Brother QL-1115NWB
     */
    QL_1115NWB("QL-1115NWB", 0x20ab, 0x45, false, 150, 35433, true, false),

    /**
     * Unknown printer
     */
    UNKNOWN(Rx.msg("model.unknown"), 0, 0, false, 0, 0, true, false);

    private static final Map<Integer, BrotherQLModel> USB_PRODUCT_ID_MAP = new HashMap<>();
    private static final Map<Integer, BrotherQLModel> MODEL_CODE_MAP = new HashMap<>();

    static {
        for (BrotherQLModel mt : BrotherQLModel.values()) {
            USB_PRODUCT_ID_MAP.put(mt.usbProductId, mt);
            MODEL_CODE_MAP.put(mt.modelCode, mt);
        }
    }

    /**
     * The model of the printer.
     */
    public final String name;

    /**
     * The USB product id of the printer model.
     */
    public final int usbProductId;

    /**
     * The model code, as given by a status information response.
     */
    public final int modelCode;
    
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

    BrotherQLModel(String name, Integer usbProductId, int modelCode, boolean allowsFeedMargin,
                   int clMinLengthPx, int clMaxLengthPx,
                   boolean rasterOnly, boolean dpi600) {
        this.name = name;
        this.usbProductId = usbProductId;
        this.modelCode = modelCode;
        this.allowsFeedMargin = allowsFeedMargin;
        this.clMinLengthPx = clMinLengthPx;
        this.clMaxLengthPx = clMaxLengthPx;
        this.rasterOnly = rasterOnly;
        this.dpi600 = dpi600;
    }

    /**
     * Identify the printer based on the USB product Id.
     *
     * @param usbProductId the usb product id
     * @return the printer model
     */
    public static BrotherQLModel fromUsbProductId(int usbProductId) {
        BrotherQLModel model = USB_PRODUCT_ID_MAP.get(usbProductId);
        return model == null ? UNKNOWN : model;
    }

    /**
     * Identify the printer based on the status model code.
     *
     * @param modelCode the model code
     * @return the printer model
     */
    public static BrotherQLModel fromModelCode(int modelCode) {
        BrotherQLModel model = MODEL_CODE_MAP.get(modelCode);
        return model == null ? UNKNOWN : model;
    }

    @Override
    public String toString() {
        return name;
    }
}
