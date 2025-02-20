/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

/**
 * Brother QL media definitions
 *
 * @author Cedric de Launois
 */
public enum BrotherQLMedia {

    // Continuous length tape 720 pins
    CT_12_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 12, 0, 106, 0, 585, 29, 90),
    CT_29_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 29, 0, 306, 0, 408, 6, 90),
    CT_38_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 38, 0, 413, 0, 295, 12, 90),
    CT_50_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 50, 0, 554, 0, 154, 12, 90),
    CT_54_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 54, 0, 590, 0, 130, 0, 90),
    CT_62_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 62, 0, 696, 0, 12, 12, 90),

    // Die-cut labels 720 pins
    DC_17X54_720(BrotherQLMediaType.DIE_CUT_LABEL, 17, 54, 165, 566, 555, 0, 90),
    DC_17X87_720(BrotherQLMediaType.DIE_CUT_LABEL, 17, 87, 165, 956, 555, 0, 90),
    DC_23X23_720(BrotherQLMediaType.DIE_CUT_LABEL, 23, 23, 236, 202, 442, 42, 90),
    DC_29X90_720(BrotherQLMediaType.DIE_CUT_LABEL, 29, 90, 306, 991, 408, 6, 90),
    DC_38X90_720(BrotherQLMediaType.DIE_CUT_LABEL, 38, 90, 413, 991, 295, 12, 90),
    DC_39X48_720(BrotherQLMediaType.DIE_CUT_LABEL, 39, 48, 425, 495, 289, 6, 90),
    DC_52X29_720(BrotherQLMediaType.DIE_CUT_LABEL, 52, 29, 578, 271, 142, 0, 90),
    DC_62X29_720(BrotherQLMediaType.DIE_CUT_LABEL, 62, 29, 696, 271, 12, 12, 90),
    DC_62X100_720(BrotherQLMediaType.DIE_CUT_LABEL, 62, 100, 696, 1109, 12, 12, 90),
    DC_12_DIA_720(BrotherQLMediaType.DIE_CUT_LABEL, 12, 12, 94, 94, 513, 113, 90),
    DC_24_DIA_720(BrotherQLMediaType.DIE_CUT_LABEL, 24, 24, 236, 236, 442, 42, 90),
    DC_58_DIA_720(BrotherQLMediaType.DIE_CUT_LABEL, 58, 58, 618, 618, 51, 51, 90),

    // Continuous length tape 1296 pins
    CT_12_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 12, 0, 106, 0, 1116, 74, 162),
    CT_50_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 50, 0, 554, 0, 686, 56, 162),
    CT_38_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 38, 0, 413, 0, 827, 56, 162),
    CT_54_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 54, 0, 590, 0, 662, 44, 162),
    CT_29_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 29, 0, 306, 0, 940, 50, 162),
    CT_62_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 62, 0, 696, 0, 544, 56, 162),
    CT_102_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 102, 0, 1164, 0, 76, 56, 162),

    // Die-cut labels 1296 pins
    DC_17X54_1296(BrotherQLMediaType.DIE_CUT_LABEL, 17, 54, 165, 566, 1087, 44, 162),
    DC_17X87_1296(BrotherQLMediaType.DIE_CUT_LABEL, 17, 87, 165, 956, 1087, 44, 162),
    DC_23X23_1296(BrotherQLMediaType.DIE_CUT_LABEL, 23, 23, 236, 202, 976, 84, 162),
    DC_29X90_1296(BrotherQLMediaType.DIE_CUT_LABEL, 29, 90, 306, 991, 940, 50, 162),
    DC_38X90_1296(BrotherQLMediaType.DIE_CUT_LABEL, 38, 90, 413, 991, 827, 56, 162),
    DC_39X48_1296(BrotherQLMediaType.DIE_CUT_LABEL, 39, 48, 425, 495, 821, 50, 162),
    DC_52X29_1296(BrotherQLMediaType.DIE_CUT_LABEL, 52, 29, 578, 271, 674, 44, 162),
    DC_62X29_1296(BrotherQLMediaType.DIE_CUT_LABEL, 62, 29, 696, 271, 544, 56, 162),
    DC_62X100_1296(BrotherQLMediaType.DIE_CUT_LABEL, 62, 100, 696, 1109, 544, 56, 162),
    DC_102X51_1296(BrotherQLMediaType.DIE_CUT_LABEL, 102, 51, 1164, 526, 76, 56, 162),
    DC_102X152_1296(BrotherQLMediaType.DIE_CUT_LABEL, 102, 153, 1164, 1660, 76, 56, 162),
    DC_12_DIA_1296(BrotherQLMediaType.DIE_CUT_LABEL, 12, 12, 94, 94, 1046, 156, 162),
    DC_24_DIA_1296(BrotherQLMediaType.DIE_CUT_LABEL, 24, 24, 236, 236, 975, 85, 162),
    DC_58_DIA_1296(BrotherQLMediaType.DIE_CUT_LABEL, 58, 58, 618, 618, 584, 94, 162);

    public final BrotherQLMediaType mediaType;
    public final int labelWidthMm;
    public final int labelLengthMm;
    public final int leftMarginPx;
    public final int bodyWidthPx;
    public final int rightMarginPx;
    public final int rgtSizeBytes;
    public final int bodyLengthPx;

    BrotherQLMedia(BrotherQLMediaType mediaType, int labelWidthMm, int labelLengthMm, int bodyWidthPx, int bodyLengthPx, 
                   int marginLeftPx, int marginRightPx, int rgtSizeBytes) {
        this.mediaType = mediaType;
        this.labelWidthMm = labelWidthMm;
        this.labelLengthMm = labelLengthMm;
        this.leftMarginPx = marginLeftPx;
        this.bodyWidthPx = bodyWidthPx;
        this.rightMarginPx = marginRightPx;
        this.rgtSizeBytes = rgtSizeBytes;
        this.bodyLengthPx = bodyLengthPx;
    }

    public static BrotherQLMedia identify(BrotherQLStatus status) {
        return BrotherQLMedia.identify(status.getPrinterId(), status.getMediaType(), status.getMediaWidth(), status.getMediaLength());
    }
    
    public static BrotherQLMedia identify(BrotherQLPrinterId printerId, BrotherQLMediaType mediaType, int labelWidthMm, int labelLengthMm) {
        int rgtSizeByte = 90;
        if (BrotherQLPrinterId.QL_1050.equals(printerId) || BrotherQLPrinterId.QL_1060N.equals(printerId)) {
            rgtSizeByte = 162;
        }

        for (BrotherQLMedia def : BrotherQLMedia.values()) {
            if (def.rgtSizeBytes == rgtSizeByte
                    && def.mediaType == mediaType
                    && def.labelWidthMm == labelWidthMm
                    && def.labelLengthMm == labelLengthMm) {
                return def;
            }
        }
        return null;
    }

}
