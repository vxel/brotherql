/*
 * Copyright (C) 2024 Cédric de Launois
 * See LICENSE for licensing information.
 *
 * Java USB Driver for printing with Brother QL printers.
 */
package org.delaunois.brotherql.util;

import java.nio.ByteBuffer;

/**
 * Internal utility to convert byte array to hex String 
 *
 * @author Cedric de Launois
 */
public class Hex {
    
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toString(ByteBuffer buffer) {
        byte[] status = new byte[buffer.limit()];
        buffer.get(status);
        buffer.rewind();
        return String.valueOf(toChar(status));
    }

    public static String toString(byte[] bytes) {
        return String.valueOf(toChar(bytes));
    }
    
    public static char[] toChar(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return hexChars;
    }
    
}
