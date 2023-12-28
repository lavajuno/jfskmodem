package org.lavajuno.jfskmodem.io;

import javax.sound.sampled.*;
import java.util.Vector;

/**
 * SoundOutput represents a single audio output.
 */
public class SoundOutput {
    private final SourceDataLine LINE;

    /**
     * Constructs a SoundOutput.
     * @throws LineUnavailableException If the output line could not be created
     */
    public SoundOutput() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        LINE = (SourceDataLine) AudioSystem.getLine(info);
        LINE.open(format);
        LINE.start();
    }

    public void play(Vector<Byte> frames) {
        byte[] buffer = createBuffer(frames);
        LINE.write(buffer, 0, buffer.length);
        LINE.drain();
    }

    public void close() {
        LINE.stop();
        LINE.close();
    }

    private static byte[] createBuffer(Vector<Byte> frames) {
        byte[] buffer = new byte[frames.size()];
        for(int i = 0; i < buffer.length; i++) { buffer[i] = frames.get(i); }
        return buffer;
    }
}
