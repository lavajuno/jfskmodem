package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.io.SoundInput;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.Vector;

public class Receiver {
    private final int SIGNAL_START_THRESHOLD;
    private final int SIGNAL_END_THRESHOLD;

    private final int BAUD_RATE;
    private final int BIT_FRAMES;
    private final SoundInput SOUND_IN;

    public Receiver(int baud_rate) throws LineUnavailableException {
        BAUD_RATE = baud_rate;
        BIT_FRAMES = 48000 / baud_rate;
        SIGNAL_START_THRESHOLD = 18000;
        SIGNAL_END_THRESHOLD = 14000;
        SOUND_IN = new SoundInput();
    }

    public byte[] receive() {
        return new byte[]{};
    }

    private Vector<Short> record(int timeout_frames) {
        Vector<Short> recorded_signal = new Vector<>();
        SOUND_IN.start();
        /* Wait for signal */
        for(int i = 0; i < timeout_frames;) {
            Vector<Short> frames = SOUND_IN.listen();
            if(Waveforms.getAmplitude(frames) > SIGNAL_START_THRESHOLD) {
                /* Start of signal */
                recorded_signal.addAll(frames);
                break;
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

    private static Vector<Short> amplify(Vector<Short> frames) {
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
