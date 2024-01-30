package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundInput;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Receiver {
    private static final int CLOCK_SCAN_WIDTH = 4096;

    private final int SIGNAL_START_THRESHOLD;
    private final int SIGNAL_END_THRESHOLD;
    private final int BIT_FRAMES;
    private final List<Short> TONE_SPACE;
    private final List<Short> TONE_MARK;
    private final List<Short> TS_CYCLE;

    private final SoundInput sound_in;

    public Receiver(int baud_rate, int signal_start_threshold, int signal_end_threshold)
            throws LineUnavailableException {
        BIT_FRAMES = 48000 / baud_rate;
        SIGNAL_START_THRESHOLD = signal_start_threshold;
        SIGNAL_END_THRESHOLD = signal_end_threshold;
        TONE_SPACE = Waveforms.getSpaceTone(baud_rate);
        TONE_MARK = Waveforms.getMarkTone(baud_rate);
        TS_CYCLE = Waveforms.getTrainingCycle(baud_rate);
        sound_in = new SoundInput();
    }

    public Receiver(int baud_rate) throws LineUnavailableException {
        this(baud_rate, 18000, 14000);
    }

    public byte[] receive(int timeout) {
        List<Short> rec_frames = record(timeout * 48000);
        List<Byte> rec_bits = decodeBits(rec_frames);
        return decodeBytes(rec_bits);
    }

    /**
     * Records a signal and returns it as a list of frames
     * @param timeout_frames Recording timeout in frames
     * @return Recorded signal as a list of frames
     */
    private List<Short> record(int timeout_frames) {
        ArrayList<Short> recorded_signal = new ArrayList<>();
        sound_in.start();
        /* Wait for signal */
        for(int i = 0; i < timeout_frames;) {
            List<Short> frames = sound_in.listen();
            if(Waveforms.getAmplitude(frames) > SIGNAL_START_THRESHOLD) {
                recorded_signal.addAll(frames);
                break; /* Start of signal */
            }
            i += frames.size();
        }
        /* Record signal */
        while(true) {
            List<Short> frames = sound_in.listen();
            recorded_signal.addAll(frames);
            if(Waveforms.getAmplitude(frames) < SIGNAL_END_THRESHOLD) {
                break; /* End of signal */
            }
        }
        return recorded_signal;
    }

    /**
     * Decodes bytes from a list of bits (including parity bits).
     * @param rec_bits Bits to decode
     * @return Decoded bytes
     */
    private byte[] decodeBytes(List<Byte> rec_bits) {
        if(rec_bits.size() % 14 != 0) {
            System.err.println("(jfskmodem) Receiver: Could not decode.");
            return new byte[]{};
        }
        int n_bytes = rec_bits.size() / 14;
        byte[] rec_bytes = new byte[n_bytes];
        byte[] current_byte = new byte[14];
        for(int i = 0; i < rec_bits.size(); i++) {
            if(i % 14 == 0 && i > 0) {
                rec_bytes[(i / 14) - 1] = Hamming.decodeByte(current_byte);
                current_byte[0] = rec_bits.get(i);
            } else {
                current_byte[i % 14] = rec_bits.get(i);
            }
        }
        rec_bytes[n_bytes - 1] = Hamming.decodeByte(current_byte);
        return rec_bytes;
    }

    /**
     * Decodes bits from a list of recorded frames.
     * @param frames Frames to decode
     * @return Decoded bits
     */
    private List<Byte> decodeBits(List<Short> frames) {
        // Recover clock index
        int i = recoverClockIndex(frames);
        if(i == -1) { return new ArrayList<>();}
        ArrayList<Byte> rec_bits = new ArrayList<>();

        // Skip past training sequence
        byte[] training_bits = {0,0,0,0};
        for(; i < frames.size() - BIT_FRAMES; i += BIT_FRAMES) {
            List<Short> bit_frames = frames.subList(i, i + BIT_FRAMES);
            if(scanTraining(training_bits, decodeBit(bit_frames))) {
                i += BIT_FRAMES;
                break; // training sequence terminated
            }
        }

        // Decode and store received bits
        for(; i < frames.size() - BIT_FRAMES; i += BIT_FRAMES) {
            List<Short> bit_frames = frames.subList(i, i + BIT_FRAMES);
            if(Waveforms.getAmplitude(bit_frames) < SIGNAL_END_THRESHOLD) {
                break; // End of signal
            }
            rec_bits.add(decodeBit(bit_frames));
        }

        return rec_bits;
    }

    /**
     * Decodes a single bit from a list of frames.
     * @param frames Frames to decode
     * @return Decoded bit
     */
    private byte decodeBit(List<Short> frames) {
        List<Short> amp_frames = amplify(frames);
        int space_diff = Waveforms.getDiff(TONE_SPACE, amp_frames);
        int mark_diff = Waveforms.getDiff(TONE_MARK, amp_frames);
        return (byte) (mark_diff < space_diff ? 1 : 0);
    }

    /**
     * Recovers the clock from a training sequence
     * @param frames List of frames containing training sequence
     * @return Index of the best match for clock recovery
     */
    private int recoverClockIndex(List<Short> frames) {
        if(frames.size() < CLOCK_SCAN_WIDTH) {
            System.err.println("(jfskmodem) Receiver: Could not recover clock.");
            return -1;
        }
        ArrayList<Integer> scan_diffs = new ArrayList<>();
        for(int i = 0; i < CLOCK_SCAN_WIDTH; i++) {
            scan_diffs.add(Waveforms.getDiff(TS_CYCLE, frames.subList(i, i + BIT_FRAMES * 2)));
        }
        int min_diff = scan_diffs.get(0);
        int min_index = 0;
        for(int i = 0; i < scan_diffs.size(); i++) {
            if(scan_diffs.get(i) < min_diff) {
                min_index = i;
                min_diff = scan_diffs.get(i);
            }
        }
        return min_index;
    }

    /**
     * Updates a sliding window of training sequence bits with the given
     * current bit, and returns true if the window matches the training sequence terminator.
     * @param seq Training sequence window (byte[4])
     * @param current_bit Current bit to push into the window
     * @return True if the window matches the training sequence terminator
     */
    private static boolean scanTraining(byte[] seq, byte current_bit) {
        for(int i = 1; i < 4; i++) {
            seq[i - 1] = seq[i];
        }
        seq[3] = current_bit;
        return seq[0] == 1 && seq[1] == 0 && seq[2] == 0 && seq[3] == 0;
    }

    /**
     * Amplifies a received signal
     * @param frames List of frames
     * @return Amplified list of frames
     */
    private static List<Short> amplify(List<Short> frames) {
        ArrayList<Short> output = new ArrayList<>(frames.size());
        for (Short frame : frames) {
            if (frame > 512) {
                output.add((short) 32767);
            } else if (frame < -512) {
                output.add((short) -32768);
            } else {
                output.add((short) 0);
            }
        }
        return output;
    }
}
