package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundOutput;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.Arrays;
import java.util.Vector;

public class Transmitter {
    private final int BAUD_RATE;
    private final int BIT_FRAMES;
    private final Vector<Short> TONE_SPACE;
    private final Vector<Short> TONE_MARK;
    private final Vector<Short> TRAINING_CYCLE;
    private final SoundOutput SOUND_OUT;


    public Transmitter(int BAUD_RATE) throws LineUnavailableException {
        this.BAUD_RATE = BAUD_RATE;
        this.BIT_FRAMES = 48000 / BAUD_RATE;
        TONE_SPACE = Waveforms.getSpaceTone(BAUD_RATE);
        TONE_MARK = Waveforms.getMarkTone(BAUD_RATE);
        TRAINING_CYCLE = Waveforms.getTrainingCycle(BAUD_RATE);
        SOUND_OUT = new SoundOutput();
    }

    public void transmit(byte[] data) {
        Vector<Short> frames = new Vector<>();
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
