package org.lavajuno.jfskmodem.io;

import org.lavajuno.jfskmodem.log.Log;

import javax.sound.sampled.*;
import java.util.List;

/**
 * SoundOutput provides functionality for writing frames to the default audio output device.
 */
@SuppressWarnings("unused")
public class SoundOutput {
    private final SourceDataLine line;
    private final Log log;

    /**
     * Constructs a SoundOutput that listens on the default input device.
     * @throws LineUnavailableException If the output line could not be created
     */
    public SoundOutput() throws LineUnavailableException {
        log = new Log("SoundOutput", Log.Level.DEBUG);
        log.debug("Opening line to default audio output device...");
        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        log.debug("Done setting up audio output.");
    }

    /**
     * Blocks and plays on the audio output device.
     * @param frames List of frames to play
     */
    public void play(List<Short> frames) {
        log.debug("Playing " + frames.size() + " frames.");
        byte[] buffer = framesToBytes(frames);
        line.flush();
        line.write(buffer, 0, buffer.length);
        line.drain();
    }

    /**
     * Flushes the output buffer.
     */
    public void flush() { line.flush(); }

    /**
     * Stops and closes the line.
     */
    public void close() {
        log.debug("Closing audio output line.");
        line.stop();
        line.close();
    }

    /**
     * @param frames List of frames as signed shorts to convert
     * @return Array of frames as bytes
     */
    private static byte[] framesToBytes(List<Short> frames) {
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
