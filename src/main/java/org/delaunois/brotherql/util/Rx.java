/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql.util;

import java.util.ResourceBundle;

/**
 * Utility class for accessing message resource bundle.
 *
 * @author Cedric de Launois
 */
public class Rx {

    private static final ResourceBundle RX = ResourceBundle
            .getBundle("org.delaunois.brotherql.BrotherQLResource");

    private Rx() {
        // Prevent instanciation
    }

    /**
     * Returns the value linked to the given key, from the resource bundle.
     *
     * @param key the key
     * @return the value
     */
    public static String msg(String key) {
        return RX.getString(key);
    }
}
