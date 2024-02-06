package org.lavajuno.jfskmodem.io;

import org.lavajuno.jfskmodem.log.Log;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

/**
 * SoundInput provides functionality for reading frames from the default audio input device.
 */
public class SoundInput {
    private static final int INPUT_BLOCK_SIZE = 4096;

    private final TargetDataLine line;
    private final Log log;

    /**
     * Constructs a SoundInput that listens on the default input device.
     * @param log_level Log level for this SoundInput
     * @throws LineUnavailableException If the input line could not be created
     */
    public SoundInput(Log.Level log_level) throws LineUnavailableException {
        log = new Log("SoundInput", log_level);
        log.debug("Opening line to default audio input device...");
        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        log.debug("Done setting up audio input.");
    }

    /**
     * Constructs a SoundInput that listens on the default input device.
     * @throws LineUnavailableException If the input line could not be created
     */
    public SoundInput() throws LineUnavailableException {
        this(Log.Level.WARN);
    }

    /**
     * Blocks and listens on the audio input device.
     * @return List of frames from the input buffer
     */
    public List<Short> listen() {
        byte[] buffer = new byte[INPUT_BLOCK_SIZE];
        line.read(buffer, 0, buffer.length);
        return bytesToFrames(buffer);
    }

    /**
     * Temporarily stops the line and flushes the input buffer.
     */
    public void stop() {
        log.debug("Stopping audio input line.");
        line.stop();
        line.flush();
    }

    /**
     * Starts the line and flushes the input buffer.
     */
    public void start() {
        log.debug("Starting audio input line.");
        line.flush();
        line.start();
    }

    /**
     * Flushes the input buffer.
     */
    public void flush() { line.flush(); }

    /**
     * Stops and closes the line.
     */
    public void close() {
        log.debug("Closing audio input line.");
        line.stop();
        line.close();
    }

    /**
     * @param buffer Array of frames as bytes to convert
     * @return List of frames as signed shorts
     */
    private static List<Short> bytesToFrames(byte[] buffer) {
        ArrayList<Short> out_frames = new ArrayList<>(buffer.length / 2);
        byte[] frame = new byte[2];
        for(int i = 0; i < buffer.length - 1; i += 2) {
            frame[0] = buffer[i];
            frame[1] = buffer[i + 1];
            out_frames.add(ByteBuffer.wrap(frame).getShort());
        }
        return out_frames;
    }
}
