/*
 *
 *  Copyright © 2013 Nayuki Minase
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  * The above copyright notice and this permission notice shall be included in
 *    all copies or substantial portions of the Software.
 *
 *  * The Software is provided "as is", without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other
 *    liability, whether in an action of contract, tort or otherwise, arising from,
 *    out of or in connection with the Software or the use or other dealings in the
 *    Software.
 */

package org.delaunois.brotherql.util;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A stream where bits can be written to. Because they are written to an underlying byte stream,
 * the end of the stream is padded with 0's up to a multiple of 8 bits. The bits are written in big endian.
 */
public final class BitOutputStream {

    // Underlying byte stream to write to.
    private OutputStream output;

    // The accumulated bits for the current byte. Always in the range 0x00 to 0xFF.
    private int currentByte;

    // The number of accumulated bits in the current byte. Always between 0 and 7 (inclusive).
    private int numBitsInCurrentByte;


    /**
     * Creates a bit output stream based on the given byte output stream.
     *
     * @param out thr output stream
     */
    public BitOutputStream(OutputStream out) {
        if (out == null)
            throw new NullPointerException("Argument is null");
        output = out;
        currentByte = 0;
        numBitsInCurrentByte = 0;
    }


    /**
     * Writes a bit to the stream. The specified bit must be 0 or 1.
     *
     * @param b the bit
     * @throws IOException if an I/O error occurs. In particular,
     *                     an {@code IOException} may be thrown if the
     *                     output stream has been closed.
     */
    public void write(int b) throws IOException {
        if (!(b == 0 || b == 1))
            throw new IllegalArgumentException("Argument must be 0 or 1");
        currentByte = currentByte << 1 | b;
        numBitsInCurrentByte++;
        if (numBitsInCurrentByte == 8) {
            output.write(currentByte);
            numBitsInCurrentByte = 0;
        }
    }

    /**
     * Closes this stream and the underlying OutputStream. If called when this bit stream is not at a byte boundary,
     * then the minimum number of "0" bits (between 0 and 7 of them) are written as padding to reach the next
     * byte boundary.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        while (numBitsInCurrentByte != 0)
            write(0);
        output.close();
    }

}