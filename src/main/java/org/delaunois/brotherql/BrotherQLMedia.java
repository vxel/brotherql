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

    /**
     * Continuous tape 12 mmm wide for 720 pins printers
     */
    CT_12_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 12, 0, 106, 0, 585, 29, 90),

    /**
     * Continuous tape 29 mmm wide for 720 pins printers
     */
    CT_29_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 29, 0, 306, 0, 408, 6, 90),

    /**
     * Continuous tape 38 mmm wide for 720 pins printers
     */
    CT_38_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 38, 0, 413, 0, 295, 12, 90),

    /**
     * Continuous tape 50 mmm wide for 720 pins printers
     */
    CT_50_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 50, 0, 554, 0, 154, 12, 90),

    /**
     * Continuous tape 54 mmm wide for 720 pins printers
     */
    CT_54_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 54, 0, 590, 0, 130, 0, 90),

    /**
     * Continuous tape 62 mmm wide for 720 pins printers
     */
    CT_62_720(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 62, 0, 696, 0, 12, 12, 90),


    /**
     * Die-cut labels 17 x 54 mmm for 720 pins printers
     */
    DC_17X54_720(BrotherQLMediaType.DIE_CUT_LABEL, 17, 54, 165, 566, 555, 0, 90),

    /**
     * Die-cut labels 17 x 87 mmm for 720 pins printers
     */
    DC_17X87_720(BrotherQLMediaType.DIE_CUT_LABEL, 17, 87, 165, 956, 555, 0, 90),

    /**
     * Die-cut labels 23 x 23 mmm for 720 pins printers
     */
    DC_23X23_720(BrotherQLMediaType.DIE_CUT_LABEL, 23, 23, 236, 202, 442, 42, 90),

    /**
     * Die-cut labels 29 x 90 mmm for 720 pins printers
     */
    DC_29X90_720(BrotherQLMediaType.DIE_CUT_LABEL, 29, 90, 306, 991, 408, 6, 90),

    /**
     * Die-cut labels 38 x 90 mmm for 720 pins printers
     */
    DC_38X90_720(BrotherQLMediaType.DIE_CUT_LABEL, 38, 90, 413, 991, 295, 12, 90),

    /**
     * Die-cut labels 39 x 48 mmm for 720 pins printers
     */
    DC_39X48_720(BrotherQLMediaType.DIE_CUT_LABEL, 39, 48, 425, 495, 289, 6, 90),

    /**
     * Die-cut labels 52 x 29 mmm for 720 pins printers
     */
    DC_52X29_720(BrotherQLMediaType.DIE_CUT_LABEL, 52, 29, 578, 271, 142, 0, 90),

    /**
     * Die-cut labels 62 x 29 mmm for 720 pins printers
     */
    DC_62X29_720(BrotherQLMediaType.DIE_CUT_LABEL, 62, 29, 696, 271, 12, 12, 90),

    /**
     * Die-cut labels 62 x 100 mmm for 720 pins printers
     */
    DC_62X100_720(BrotherQLMediaType.DIE_CUT_LABEL, 62, 100, 696, 1109, 12, 12, 90),

    /**
     * Die-cut labels 12 mm DIA for 720 pins printers
     */
    DC_12_DIA_720(BrotherQLMediaType.DIE_CUT_LABEL, 12, 12, 94, 94, 513, 113, 90),

    /**
     * Die-cut labels 24 mm DIA for 720 pins printers
     */
    DC_24_DIA_720(BrotherQLMediaType.DIE_CUT_LABEL, 24, 24, 236, 236, 442, 42, 90),

    /**
     * Die-cut labels 58 mm DIA for 720 pins printers
     */
    DC_58_DIA_720(BrotherQLMediaType.DIE_CUT_LABEL, 58, 58, 618, 618, 51, 51, 90),


    /**
     * Continuous tape 12 mmm wide for 1296 pins printers
     */
    CT_12_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 12, 0, 106, 0, 1116, 74, 162),

    /**
     * Continuous tape 29 mmm wide for 1296 pins printers
     */
    CT_29_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 29, 0, 306, 0, 940, 50, 162),

    /**
     * Continuous tape 38 mmm wide for 1296 pins printers
     */
    CT_38_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 38, 0, 413, 0, 827, 56, 162),

    /**
     * Continuous tape 50 mmm wide for 1296 pins printers
     */
    CT_50_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 50, 0, 554, 0, 686, 56, 162),

    /**
     * Continuous tape 54 mmm wide for 1296 pins printers
     */
    CT_54_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 54, 0, 590, 0, 662, 44, 162),

    /**
     * Continuous tape 62 mmm wide for 1296 pins printers
     */
    CT_62_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 62, 0, 696, 0, 544, 56, 162),

    /**
     * Continuous tape 102 mmm wide for 1296 pins printers
     */
    CT_102_1296(BrotherQLMediaType.CONTINUOUS_LENGTH_TAPE, 102, 0, 1164, 0, 76, 56, 162),


    /**
     * Die-cut labels 17 x 54 mmm for 1296 pins printers
     */
    DC_17X54_1296(BrotherQLMediaType.DIE_CUT_LABEL, 17, 54, 165, 566, 1087, 44, 162),

    /**
     * Die-cut labels 17 x 87 mmm for 1296 pins printers
     */
    DC_17X87_1296(BrotherQLMediaType.DIE_CUT_LABEL, 17, 87, 165, 956, 1087, 44, 162),

    /**
     * Die-cut labels 23 x 23 mmm for 1296 pins printers
     */
    DC_23X23_1296(BrotherQLMediaType.DIE_CUT_LABEL, 23, 23, 236, 202, 976, 84, 162),

    /**
     * Die-cut labels 29 x 90 mmm for 1296 pins printers
     */
    DC_29X90_1296(BrotherQLMediaType.DIE_CUT_LABEL, 29, 90, 306, 991, 940, 50, 162),

    /**
     * Die-cut labels 38 x 90 mmm for 1296 pins printers
     */
    DC_38X90_1296(BrotherQLMediaType.DIE_CUT_LABEL, 38, 90, 413, 991, 827, 56, 162),

    /**
     * Die-cut labels 39 x 48 mmm for 1296 pins printers
     */
    DC_39X48_1296(BrotherQLMediaType.DIE_CUT_LABEL, 39, 48, 425, 495, 821, 50, 162),

    /**
     * Die-cut labels 52 x 29 mmm for 1296 pins printers
     */
    DC_52X29_1296(BrotherQLMediaType.DIE_CUT_LABEL, 52, 29, 578, 271, 674, 44, 162),

    /**
     * Die-cut labels 62 x 29 mmm for 1296 pins printers
     */
    DC_62X29_1296(BrotherQLMediaType.DIE_CUT_LABEL, 62, 29, 696, 271, 544, 56, 162),

    /**
     * Die-cut labels 62 x 100 mmm for 1296 pins printers
     */
    DC_62X100_1296(BrotherQLMediaType.DIE_CUT_LABEL, 62, 100, 696, 1109, 544, 56, 162),

    /**
     * Die-cut labels 102 x 51 mmm for 1296 pins printers
     */
    DC_102X51_1296(BrotherQLMediaType.DIE_CUT_LABEL, 102, 51, 1164, 526, 76, 56, 162),

    /**
     * Die-cut labels 102 x 152 mmm for 1296 pins printers
     */
    DC_102X152_1296(BrotherQLMediaType.DIE_CUT_LABEL, 102, 153, 1164, 1660, 76, 56, 162),

    /**
     * Die-cut labels 12 mm DIA for 720 pins printers
     */
    DC_12_DIA_1296(BrotherQLMediaType.DIE_CUT_LABEL, 12, 12, 94, 94, 1046, 156, 162),

    /**
     * Die-cut labels 24 mm DIA for 720 pins printers
     */
    DC_24_DIA_1296(BrotherQLMediaType.DIE_CUT_LABEL, 24, 24, 236, 236, 975, 85, 162),

    /**
     * Die-cut labels 58 mm DIA for 720 pins printers
     */
    DC_58_DIA_1296(BrotherQLMediaType.DIE_CUT_LABEL, 58, 58, 618, 618, 584, 94, 162);


    /**
     * The media type.
     */
    public final BrotherQLMediaType mediaType;

    /**
     * The label width in mm.
     */
    public final int labelWidthMm;

    /**
     * The label length/height in mm.
     */
    public final int labelLengthMm;

    /**
     * The left margin in dots.
     */
    public final int leftMarginPx;

    /**
     * The body width in dots.
     */
    public final int bodyWidthPx;

    /**
     * The right margin in dots.
     */
    public final int rightMarginPx;

    /**
     * Number of Raster Graphic Transfer Bytes,
     * i.e. number of bytes sent per printed line.
     */
    public final int rgtSizeBytes;

    /**
     * The body length/height in dots.
     */
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

    /**
     * Extract the media based on the given printer status.
     *
     * @param status the status
     * @return the media
     */
    public static BrotherQLMedia identify(BrotherQLStatus status) {
        BrotherQLModel model = status.getModel();
        BrotherQLMediaType mediaType = status.getMediaType();
        int labelWidthMm = status.getMediaWidth();
        int labelLengthMm = status.getMediaLength();

        int rgtSizeByte = 90;
        if (BrotherQLModel.QL_1050.equals(model) || BrotherQLModel.QL_1060N.equals(model)) {
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
