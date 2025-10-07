/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A job for a Brother QL printer.
 * If multiple images are provided, options will apply to all.
 *
 * @author Cedric de Launois
 */
@Getter
@Setter
@Accessors(chain = true)
public class BrotherQLJob {

    /**
     * Construct a new print job.
     */
    public BrotherQLJob() {
    }

    /**
     * Whether autocut is enabled or not.
     */
    private boolean autocut = true;

    /**
     * Whether half cut is enabled or not.
     */
    private boolean halfcut = false;

    /**
     * The number of labels after which a cut is applied.
     */
    private int cutEach = 1;

    /**
     * The label raster images that will be convertered to monochrome and printed.
     */
    private List<BufferedImage> images = new ArrayList<>();

    /**
     * The feed amount in dots.
     */
    private int feedAmount;

    /**
     * The delay in ms between prints.
     */
    private int delay = 0;

    /**
     * The threshold value (between 0 and 1) to discriminate between black and white pixels, based on pixel luminance.
     * Lower threshold means less printed dots, i.e. a brighter image.
     * Meaningless if dither is true.
     * Pixels with luminance below this threshold will be printed.
     * Default is 0.5.
     */
    private float threshold = 0.5f;

    /**
     * Whether to apply dithering or not when converting images to monochrome B/W.
     * If true, threshold is meaningless.
     */
    private boolean dither = true;

    /**
     * Brightness factor applied before dithering. Higher means brighter.
     * Default is 1.0. For images, it is advised to set it higher, for example 1.8f, i.e. 
     * the dithered image is made nearly 2x brighter than the original.
     * This improves the rendering of dithered images on Brother label printers because
     * printed dots tend to "bleed" on the adjacent ones, making the image darker.
     */
    private float brightness = 1.0f;

    /**
     * Rotate the image (clock-wise) by this angle in degrees.
     * Accepted angles are multiple of 90 degrees (e.g. -270, -90, 90, 180, 270).
     * Default is 0.
     */
    private int rotate = 0;

    /**
     * Use 600 dpi height x 300 dpi wide resolution. Only available on some models.
     * The image must be provided as 600x600 dpi. The width will be resized 
     * to 300dpi.
     * Default is false;
     */
    private boolean dpi600 = false;

    /**
     * Defines the target media (size, type - die-cut or endless).
     * Optional for USB printers as the media is detected and provided by the printer.
     * Required for Network printers.
     */
    private BrotherQLMedia media;

}
