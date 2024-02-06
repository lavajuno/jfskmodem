package org.lavajuno.jfskmodem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundInput;
import org.lavajuno.jfskmodem.log.Log;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Receiver manages a line to the default audio input device
 * and allows you to receive data over it.
 */
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
    private final Log log;

    /**
     * Constructs a Receiver with the given baud rate, sensitivity parameters, and log level.
     * @param baud_rate Baud rate for this Receiver
     * @param signal_start_threshold Amplitude threshold to recognize the start of a signal
     * @param signal_end_threshold Amplitude threshold to recognize the end of a signal
     * @param log_level Log level for this Receiver.
     * @throws LineUnavailableException If the audio input line could not be created
     */
    public Receiver(int baud_rate, int signal_start_threshold, int signal_end_threshold, Log.Level log_level)
            throws LineUnavailableException {
        BIT_FRAMES = 48000 / baud_rate;
        SIGNAL_START_THRESHOLD = signal_start_threshold;
        SIGNAL_END_THRESHOLD = signal_end_threshold;
        TONE_SPACE = Waveforms.getSpaceTone(baud_rate);
        TONE_MARK = Waveforms.getMarkTone(baud_rate);
        TS_CYCLE = Waveforms.getTrainingCycle(baud_rate);
        sound_in = new SoundInput();
        log = new Log("Receiver", log_level);
    }

    /**
     * Constructs a Receiver with the given baud rate and sensitivity parameters.
     * @param baud_rate Baud rate for this Receiver
     * @param signal_start_threshold Amplitude threshold to recognize the start of a signal
     * @param signal_end_threshold Amplitude threshold to recognize the end of a signal
     * @throws LineUnavailableException If the audio input line could not be created
     */
    public Receiver(int baud_rate, int signal_start_threshold, int signal_end_threshold)
            throws LineUnavailableException {
        this(baud_rate, signal_start_threshold, signal_end_threshold, Log.Level.WARN);
    }

    /**
     * Constructs a Receiver with the given baud rate and log level.
     * @param baud_rate Baud rate for this Receiver
     * @param log_level Log level for this Receiver
     * @throws LineUnavailableException If the audio input line could not be created
     */
    public Receiver(int baud_rate, Log.Level log_level) throws LineUnavailableException {
        this(baud_rate, 18000, 14000, log_level);
    }

    /**
     * Constructs a Receiver with the given baud rate.
     * @param baud_rate Baud rate for this Receiver
     * @throws LineUnavailableException If the audio input line could not be created
     */
    public Receiver(int baud_rate) throws LineUnavailableException {
        this(baud_rate, Log.Level.WARN);
    }

    /**
     * Receives and decodes bytes from this Receiver's audio input.
     * @param timeout Listen timeout in seconds
     * @return Received bytes. Empty if signal cannot be decoded or timeout is reached.
     */
    public byte[] receive(int timeout) {
        log.info("Listening...");
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
        sound_in.listen();
        /* Wait for signal */
        for(int i = 0; i < timeout_frames;) {
            List<Short> frames = sound_in.listen();
            if(Waveforms.getAmplitude(frames) > SIGNAL_START_THRESHOLD) {
                log.debug("Signal start detected.");
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
                log.debug("Signal end detected.");
                break; /* End of signal */
            }
        }
        sound_in.stop();
        log.debug("Received " + recorded_signal.size() + " frames.");
        return recorded_signal;
    }

    /**
     * Decodes bytes from a list of bits (including parity bits).
     * @param rec_bits Bits to decode
     * @return Decoded bytes
     */
    private byte[] decodeBytes(List<Byte> rec_bits) {
        if(rec_bits.size() % 14 != 0 || rec_bits.isEmpty()) {
            log.error("Could not decode (Not enough bits)");
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
        log.debug("Decoded " + rec_bytes.length + " bytes.");
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
        if(i == -1) { return new ArrayList<>(); }
        ArrayList<Byte> rec_bits = new ArrayList<>();

        // Skip past training sequence
        byte[] training_bits = {0,0,0,0};
        for(; i < frames.size() - BIT_FRAMES; i += BIT_FRAMES) {
            List<Short> bit_frames = frames.subList(i, i + BIT_FRAMES);
            if(scanTraining(training_bits, decodeBit(bit_frames))) {
                i += BIT_FRAMES;
                log.debug("Training sequence terminated on frame " + i + ".");
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

        log.debug("Decoded " + rec_bits.size() + " bits. (Including ECC)");
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
            log.warn("Could not recover clock from received signal. (Not enough information)");
            return -1;
        }
        ArrayList<Integer> scan_diffs = new ArrayList<>();
        for(int i = 0; i < CLOCK_SCAN_WIDTH - BIT_FRAMES * 2; i++) {
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
        log.debug("Recovered clock from signal. (Best match on frame " + min_index + ")");
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
