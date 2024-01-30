package org.lavajuno.jfskmodem.modem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundOutput;
import org.lavajuno.jfskmodem.JFSKModemApplication;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

public class Transmitter {
    private final int N_TS_CYCLES;
    private final List<Short> TONE_SPACE;
    private final List<Short> TONE_MARK;
    private final List<Short> TS_CYCLE;
    private final SoundOutput SOUND_OUT;


    public Transmitter(int baud_rate) throws LineUnavailableException {
        N_TS_CYCLES = (int) (baud_rate * JFSKModemApplication.TRAINING_TIME / 2);
        TONE_SPACE = Waveforms.getSpaceTone(baud_rate);
        TONE_MARK = Waveforms.getMarkTone(baud_rate);
        TS_CYCLE = Waveforms.getTrainingCycle(baud_rate);
        SOUND_OUT = new SoundOutput();
    }

    public void transmit(byte[] data) {
        ArrayList<Short> frames = new ArrayList<>();
        // Generate training sequence frames
        for(int i = 0; i < N_TS_CYCLES; i++) { frames.addAll(TS_CYCLE); }
        // Generate training sequence termination frames
        frames.addAll(TONE_MARK);
        for(int i = 0; i < 3; i++) { frames.addAll(TONE_SPACE); }
        // Generate data frames
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
        // Play frames
        SOUND_OUT.play(frames);
    }
}
