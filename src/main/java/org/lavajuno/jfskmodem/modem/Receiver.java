package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.io.SoundInput;

import javax.sound.sampled.LineUnavailableException;
import java.util.Vector;

public class Receiver {
    private final SoundInput SOUND_IN;

    public Receiver(int baud_rate) throws LineUnavailableException {
        SOUND_IN = new SoundInput();
    }

    public byte[] receive(int timeout) {

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
