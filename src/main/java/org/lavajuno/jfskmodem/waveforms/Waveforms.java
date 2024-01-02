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
        Vector<Short> res = new Vector<>(getMarkTone(baud_rate));
        res.addAll(getSpaceTone(baud_rate));
        return res;
    }

    /**
     * Gets the mean of the differences between two waveforms at each frame.
     * @param a Waveform A
     * @param b Waveform B
     * @return Mean of the differences between A and B at each frame
     */
    public static int getDiff(Vector<Short> a, Vector<Short> b) {
        if(a.size() != b.size()) {
            throw new IllegalArgumentException("Clips must be of same length.");
        }
        long total = 0;
        for(int i = 0; i < a.size(); i++) {
            total += Math.abs(a.get(i) - b.get(i));
        }
        return (int) total / a.size();
    }
}
