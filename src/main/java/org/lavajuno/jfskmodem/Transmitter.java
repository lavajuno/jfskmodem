package org.lavajuno.jfskmodem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundOutput;
import org.lavajuno.jfskmodem.log.Log;
import org.lavajuno.jfskmodem.waveforms.Waveforms;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transmitter manages a line to the default audio output device
 * and allows you to send data over it.
 */
public class Transmitter {
    private final int N_TS_CYCLES;
    private final List<Short> TONE_SPACE;
    private final List<Short> TONE_MARK;
    private final List<Short> TS_CYCLE;

    private final SoundOutput sound_out;
    private final Log log;

    /**
     * Constructs a Transmitter with the given baud rate.
     * @param baud_rate Baud rate for this Transmitter
     * @param log_level Log level for this Transmitter
     * @throws LineUnavailableException If the audio output line could not be created
     */
    public Transmitter(int baud_rate, Log.Level log_level) throws LineUnavailableException {
        N_TS_CYCLES = (int) (baud_rate * JfskModemDemo.TRAINING_TIME / 2);
        TONE_SPACE = Waveforms.getSpaceTone(baud_rate);
        TONE_MARK = Waveforms.getMarkTone(baud_rate);
        TS_CYCLE = Waveforms.getTrainingCycle(baud_rate);
        sound_out = new SoundOutput();
        log = new Log("Transmitter", log_level);
    }

    public Transmitter(int baud_rate) throws LineUnavailableException {
        this(baud_rate, Log.Level.WARN);
    }

    /**
     * Encodes and transmits the given bytes.
     * @param data Bytes to transmit
     */
    public void transmit(byte[] data) {
        log.info("Transmitting " + data.length + " bytes.");
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
        log.debug("Transmitting " + frames.size() + " frames.");
        // Play frames
        sound_out.play(frames);
    }
}
