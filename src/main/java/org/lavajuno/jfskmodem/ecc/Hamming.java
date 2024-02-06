package org.lavajuno.jfskmodem.ecc;

import java.util.Arrays;

/**
 * Hamming provides functionality for encoding
 * and decoding data with Hamming(4,3).
 */
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
     * Parity matrix to find the position of errors while decoding data
     */
    private static final byte[][] M_PARITY = {
            {1, 0, 1, 0, 1, 0, 1},
            {0, 1, 1, 0, 0, 1, 1},
            {0, 0, 0, 1, 1, 1, 1}
    };

    /**
     * Multiplies a matrix and vector modulo 2.
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
     * Encodes 4 data bits with Hamming(4,3)
     * @param data Data to encode
     * @return Encoded data
     */
    private static byte[] encode(byte[] data) { return multiply(M_GENERATOR, data); }

    /**
     * Encodes a byte to 14 encoded bits with Hamming(4,3)
     * @param data Data bits to encode
     * @return Encoded bits
     */
    public static byte[] encodeByte(byte data) {
        byte[] res = new byte[14];
        byte[] bits = byteToBits(data);
        byte[] nibble0 = encode(Arrays.copyOfRange(bits,0, 4));
        byte[] nibble1 = encode(Arrays.copyOfRange(bits, 4, 8));
        System.arraycopy(nibble0, 0, res, 0, 7);
        System.arraycopy(nibble1, 0, res, 7, 7);
        return res;
    }

    /**
     * Decodes 4 data bits from 7 received bits with Hamming(4,3)
     * @param data Bits to decode
     * @return Decoded data bits
     */
    private static byte[] decode(byte[] data) {
        byte[] syn = multiply(M_PARITY, data);
        int error_pos = syn[2] * 4 + syn[1] * 2 + syn[0];
        byte[] res = data.clone();
        if(error_pos != 0) {
            res[error_pos - 1] = (byte) (res[error_pos - 1] == 1 ? 0 : 1);
        }
        return new byte[]{res[2], res[4], res[5], res[6]};
    }

    /**
     * Decodes a byte from 14 encoded bits with Hamming(4,3)
     * @param data Bits to decode
     * @return Decoded byte
     */
    public static byte decodeByte(byte[] data) {
        byte[] res = new byte[8];
        byte[] nibble0 = decode(Arrays.copyOfRange(data, 0, 7));
        byte[] nibble1 = decode(Arrays.copyOfRange(data, 7, 14));
        System.arraycopy(nibble0, 0, res, 0, 4);
        System.arraycopy(nibble1, 0, res, 4, 4);
        return bitsToByte(res);
    }

    /**
     * Converts a byte to bits
     * @param b Byte to convert
     * @return Converted bits
     */
    private static byte[] byteToBits(byte b) {
        byte[] res = new byte[8];
        byte mask = 1;
        for(int i = 0; i < 8; i++) {
            res[7 - i] = (byte) ((b & mask) == 0 ? 0 : 1);
            mask <<= 1;
        }
        return res;
    }

    /**
     * Converts an array of bits to a byte
     * @param b Array of bits to convert
     * @return Converted byte
     */
    private static byte bitsToByte(byte[] b) {
        byte res = 0;
        byte mask = 1;
        for(int i = 0; i < 8; i++) {
            res += (b[7 - i] == 0) ? 0 : mask;
            mask <<= 1;
        }
        return res;
    }
}
