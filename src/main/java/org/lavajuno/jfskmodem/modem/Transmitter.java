package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundOutput;
import org.lavajuno.jfskmodem.main.JFSKModemApplication;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.Vector;

public class Transmitter {
    private final int N_TS_CYCLES;
    private final Vector<Short> TONE_SPACE;
    private final Vector<Short> TONE_MARK;
    private final Vector<Short> TS_CYCLE;
    private final SoundOutput SOUND_OUT;


    public Transmitter(int baud_rate) throws LineUnavailableException {
        N_TS_CYCLES = (int) (baud_rate * JFSKModemApplication.TRAINING_TIME / 2);
        TONE_SPACE = Waveforms.getSpaceTone(baud_rate);
        TONE_MARK = Waveforms.getMarkTone(baud_rate);
        TS_CYCLE = Waveforms.getTrainingCycle(baud_rate);
        SOUND_OUT = new SoundOutput();
    }

    public void transmit(byte[] data) {
        Vector<Short> frames = new Vector<>();
        /* Training sequence */
        for(int i = 0; i < N_TS_CYCLES; i++) { frames.addAll(TS_CYCLE); }
        /* Training sequence termination */
        frames.addAll(TONE_MARK);
        for(int i = 0; i < 3; i++) { frames.addAll(TONE_SPACE); }
        /* Data */
        for(byte i : data) {
            byte[] bits = Hamming.encodeByte(i);
            for(byte j : bits) {
                if(j == 0) {
                    frames.addAll(TONE_SPACE);
                } else {
                    frames.addAll(TONE_MARK);
                }
            }
        }
        SOUND_OUT.play(frames);
    }
}
