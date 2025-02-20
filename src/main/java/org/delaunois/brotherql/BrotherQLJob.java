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
 *
 * @author Cedric de Launois
 */
@Getter
@Setter
@Accessors(chain = true)
public class BrotherQLJob {

    /**
     * Whether autocut is enabled or not.
     */
    private boolean autocut = true;

    /**
     * The number of labels after which a cut is applied.
     */
    private int cutEach = 1;

    /**
     * The label raster images that will be convertered to monochrome and printed.
     */
    private List<BufferedImage> images = new ArrayList<>();

    /**
     *  The feed amount in dots.
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
     * Default is 0.35 to compensate printed dots that "bleed" on the adjacent ones.
     */
    private float threshold = 0.35f;

    /**
     * Whether to apply dithering or not when converting images to monochrome B/W.
     * If true, threshold is meaningless.
     */
    private boolean dither = true;

    /**
     * Brightness factor applied before dithering. Higher means brighter.
     * Default is 1.8, i.e. the dithered image is made nearly 2x brighter than the original. 
     * This improves the rendering of dithered images on Brother label printers because 
     * printed dots tend to "bleed" on the adjacent ones, making the image darker.
     * This does not affect images that are already pure black and white since black (0.0) remains black
     * and white (1.0) remains white.
     */
    private float brightness = 1.8f;

    /**
     * Rotate the image (clock-wise) by this angle in degrees.
     * Accepted angles are multiple of 90 degrees (e.g. -270, -90, 90, 180, 270).
     * Default is 0.
     */
    private int rotate = 0;
    
}
