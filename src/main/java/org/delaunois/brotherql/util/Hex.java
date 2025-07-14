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

    private Hex() {
        // Prevent instanciation
    }

    /**
     * Converts the given buffer content to a displayable hex String.
     *
     * @param buffer the buffer
     * @return the string
     */
    public static String toString(ByteBuffer buffer) {
        byte[] status = new byte[buffer.limit()];
        buffer.get(status);
        buffer.rewind();
        return String.valueOf(toChar(status));
    }

    /**
     * Converts the given byte array to a displayable hex String.
     *
     * @param bytes the byte array
     * @return the string
     */
    public static String toString(byte[] bytes) {
        return String.valueOf(toChar(bytes));
    }

    public static String prettyDump(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        
        for (int start = 0; start < bytes.length; start += 16) {
            sb.append(String.format("%08X:  ", start));
            for (int i = start; i < start + 8 && i < bytes.length; i++) {
                sb.append(String.format("%02X ", bytes[i]));
            }
            sb.append(" ");
            for (int i = start + 8; i < start + 16 && i < bytes.length; i++) {
                sb.append(String.format("%02X ", bytes[i]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    private static char[] toChar(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return hexChars;
    }

}
