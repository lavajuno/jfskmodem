package org.lavajuno.jfskmodem.ecc;

import java.util.Arrays;

/**
 * Hamming provides functionality for encoding
 * and decoding data with Hamming(4,3).
 */
@SuppressWarnings("unused")
public class Hamming {
    /**
     * Generator matrix to encode data
     */
    private static final byte[][] M_GENERATOR = {
            {1, 1, 0, 1},
            {1, 0, 1, 1},
            {1, 0, 0, 0},
            {0, 1, 1, 1},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
    };

    /**
     * Parity matrix to find errors while decoding data
     */
    private static final byte[][] M_PARITY = {
            {1, 0, 1, 0, 1, 0, 1},
            {0, 1, 1, 0, 0, 1, 1},
            {0, 0, 0, 1, 1, 1, 1}
    };

    /**
     * @param a Matrix
     * @param b Vector
     * @return Product of the matrix and vector (modulo 2)
     */
    private static byte[] multiply(byte[][] a, byte[] b) {
        byte[] res = new byte[a.length];
        for(int i = 0; i < a.length; i++) {
            for(int j = 0; j < b.length; j++) {
                res[i] += (byte) (a[i][j] * b[j]);
            }
            res[i] %= 2;
        }
        return res;
    }

    /**
     * Encodes data with Hamming(4,3)
     * @param data Data to encode
     * @return Encoded data
     */
    public static byte[] encode(byte[] data) { return multiply(M_GENERATOR, data); }

    /**
     * Decodes data with Hamming(4,3)
     * @param data Data to decode
     * @return Decoded data
     */
    public static byte[] decode(byte[] data) {
        byte[] syn = multiply(M_PARITY, data);
        int error_pos = syn[2] * 4 + syn[1] * 2 + syn[0];
        byte[] res = data.clone();
        if(error_pos != 0) {
            res[error_pos - 1] = (byte) (res[error_pos - 1] == 1 ? 0 : 1);
        }
        return new byte[]{res[2], res[4], res[5], res[6]};
    }
}
