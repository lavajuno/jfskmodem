package org.lavajuno.jfskmodem.waveforms;

import java.util.Vector;

/**
 * Waveforms provides functionality for generating space+mark tones and training cycles.
 */
public class Waveforms {
    /**
     * Generates a single space tone for the given baud rate.
     * @param baud_rate Baud rate to use for generation
     * @return Space tone frames as a vector of shorts
     * @throws IllegalArgumentException If the baud rate is not a factor of 48000 or not divisible by 4
     */
    public static Vector<Short> getSpaceTone(int baud_rate) throws IllegalArgumentException {
        if(48000 % baud_rate != 0 || baud_rate % 4 != 0) {
            throw new IllegalArgumentException("Invalid baud rate.");
        }
        int bit_frames = 48000 / baud_rate;
        Vector<Short> res = new Vector<>(bit_frames);
        for(int i = 0; i < bit_frames / 2; i++) {
            res.add((short) 32767);
        }
        for(int i = bit_frames / 2; i < bit_frames; i++) {
            res.add((short) -32768);
        }
        return res;
    }

    /**
     * Generates a single mark tone for the given baud rate.
     * @param baud_rate Baud rate to use for generation
     * @return Mark tone frames as a vector of shorts
     * @throws IllegalArgumentException If the baud rate is not a factor of 48000 or not divisible by 4
     */
    public static Vector<Short> getMarkTone(int baud_rate) throws IllegalArgumentException {
        if(48000 % baud_rate != 0 || baud_rate % 4 != 0) {
            throw new IllegalArgumentException("Invalid baud rate.");
        }
        Vector<Short> res = getSpaceTone(baud_rate * 2);
        res.addAll(getSpaceTone(baud_rate * 2));
        return res;
    }

    /**
     * Generates a single training cycle for the given baud rate.
     * @param baud_rate Baud rate to use for generation
     * @return Training cycle frames as a vector of shorts
     * @throws IllegalArgumentException If the baud rate is not a factor of 48000 or not divisible by 4
     */
    public static Vector<Short> getTrainingCycle(int baud_rate) throws IllegalArgumentException {
        Vector<Short> res = new Vector<>(getSpaceTone(baud_rate));
        res.addAll(getMarkTone(baud_rate));
        return res;
    }
}
