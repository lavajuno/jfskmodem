package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.io.SoundInput;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.List;
import java.util.Vector;

public class Receiver {
    private static final int CLOCK_SCAN_WIDTH = 4096;
    private final int SIGNAL_START_THRESHOLD;
    private final int SIGNAL_END_THRESHOLD;

    private final int BAUD_RATE;
    private final int BIT_FRAMES;
    private final Vector<Short> TONE_SPACE;
    private final Vector<Short> TONE_MARK;
    private final Vector<Short> TS_CYCLE;
    private final SoundInput SOUND_IN;

    public Receiver(int baud_rate, int signal_start_threshold, int signal_end_threshold)
            throws LineUnavailableException {
        BAUD_RATE = baud_rate;
        BIT_FRAMES = 48000 / baud_rate;
        SIGNAL_START_THRESHOLD = signal_start_threshold;
        SIGNAL_END_THRESHOLD = signal_end_threshold;
        TONE_SPACE = Waveforms.getSpaceTone(baud_rate);
        TONE_MARK = Waveforms.getMarkTone(baud_rate);
        TS_CYCLE = Waveforms.getTrainingCycle(baud_rate);
        SOUND_IN = new SoundInput();
    }

    public Receiver(int baud_rate) throws LineUnavailableException {
        this(baud_rate, 18000, 14000);
    }

    public byte[] receive(int timeout) {
        Vector<Short> rec_frames = record(timeout * 48000);

        return new byte[]{};
    }

    private byte[] decode(Vector<Short> frames) {
        int start_frame = recoverClockIndex(frames);
        if(start_frame == -1) { return new byte[]{}; }
        Vector<Byte> rec_bits = new Vector<>();
        for(int i = start_frame; i < frames.size() - BIT_FRAMES; i += BIT_FRAMES) {
            Vector<Short> rec_bit = (Vector<Short>) frames.subList(i, i + BIT_FRAMES);
            if(Waveforms.getAmplitude(rec_bit) < SIGNAL_END_THRESHOLD) {
                break; // End of signal
            }
            rec_bits.add(decodeBit(rec_bit));
        }
        // TODO finish
        return new byte[]{};
    }

    private byte decodeBit(List<Short> frames) {
        Vector<Short> amp_frames = amplify(frames);
        int space_diff = Waveforms.getDiff(TONE_SPACE, amp_frames);
        int mark_diff = Waveforms.getDiff(TONE_MARK, amp_frames);
        return (byte) (mark_diff < space_diff ? 1 : 0);
    }

    private int recoverClockIndex(Vector<Short> frames) {
        if(frames.size() < CLOCK_SCAN_WIDTH) { return -1; }
        Vector<Short> scan_frames = (Vector<Short>) frames.subList(0, CLOCK_SCAN_WIDTH);
        Vector<Integer> scan_diffs = new Vector<>();
        for(int i = 0; i < CLOCK_SCAN_WIDTH - BIT_FRAMES; i++) {
            scan_diffs.add(Waveforms.getDiff(TS_CYCLE, scan_frames.subList(i, i + BIT_FRAMES)));
        }
        int min_diff = scan_diffs.get(0);
        int min_index = 0;
        for(int i = 0; i < scan_diffs.size(); i++) {
            if(scan_diffs.get(i) < min_diff) {
                min_index = i;
            }
        }
        return min_index;
    }

    /**
     * Records a signal
     * @param timeout_frames
     * @return
     */
    private Vector<Short> record(int timeout_frames) {
        Vector<Short> recorded_signal = new Vector<>();
        SOUND_IN.start();
        /* Wait for signal */
        for(int i = 0; i < timeout_frames;) {
            Vector<Short> frames = SOUND_IN.listen();
            if(Waveforms.getAmplitude(frames) > SIGNAL_START_THRESHOLD) {
                recorded_signal.addAll(frames);
                break; /* Start of signal */
            }
            i += frames.size();
        }
        /* Record signal */
        while(true) {
            Vector<Short> frames = SOUND_IN.listen();
            recorded_signal.addAll(frames);
            if(Waveforms.getAmplitude(frames) < SIGNAL_END_THRESHOLD) {
                break; /* End of signal */
            }
        }
        return recorded_signal;
    }

    /**
     * Amplifies a signal
     * @param frames
     * @return
     */
    private static Vector<Short> amplify(List<Short> frames) {
        Vector<Short> res = new Vector<>(frames.size());
        for (Short frame : frames) {
            if (frame > 512) {
                res.add((short) 32767);
            } else if (frame < -512) {
                res.add((short) -32768);
            } else {
                res.add((short) 0);
            }
        }
        return res;
    }


}
