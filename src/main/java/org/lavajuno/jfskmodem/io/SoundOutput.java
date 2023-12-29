package org.lavajuno.jfskmodem.io;

import javax.sound.sampled.*;
import java.util.Vector;

/**
 * SoundOutput represents a single audio output.
 */
@SuppressWarnings("unused")
public class SoundOutput {
    private final SourceDataLine LINE;

    /**
     * Constructs a SoundOutput that listens on the default input device.
     * @throws LineUnavailableException If the output line could not be created
     */
    public SoundOutput() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        LINE = (SourceDataLine) AudioSystem.getLine(info);
        LINE.open(format);
        LINE.start();
    }

    /**
     * Blocks and plays on the audio output device.
     * @param frames Vector of frames to play
     */
    public void play(Vector<Short> frames) {
        byte[] buffer = framesToBytes(frames);
        LINE.write(buffer, 0, buffer.length);
        LINE.drain();
    }

    /**
     * Flushes the output buffer.
     */
    public void flush() { LINE.flush(); }

    /**
     * Stops and closes the line.
     */
    public void close() {
        LINE.stop();
        LINE.close();
    }

    /**
     * @param frames Vector of frames as signed shorts to convert
     * @return Array of frames as bytes
     */
    private static byte[] framesToBytes(Vector<Short> frames) {
        byte[] buffer = new byte[frames.size() * 2];
        byte[] frame = new byte[2];
        for(int i = 0; i < frames.size(); i++) {
            short fv = frames.get(i);
            frame[0] = (byte) (fv >> 8);
            frame[1] = (byte) fv;
            buffer[i * 2] = frame[0];
            buffer[i * 2 + 1] = frame[1];
        }
        return buffer;
    }
}
