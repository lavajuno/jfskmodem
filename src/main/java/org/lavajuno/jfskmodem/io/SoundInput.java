package org.lavajuno.jfskmodem.io;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * SoundInput represents a single audio input device.
 */
@SuppressWarnings("unused")
public class SoundInput {
    private static final int INPUT_BLOCK_SIZE = 4096;
    private final TargetDataLine LINE;

    /**
     * Constructs a SoundInput that listens on the default input device.
     * @throws LineUnavailableException If the input line could not be created
     */
    public SoundInput() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        LINE = (TargetDataLine) AudioSystem.getLine(info);
        LINE.open(format);
    }

    /**
     * Blocks and listens on the audio input device.
     * @return Vector of frames from the input buffer
     */
    public Vector<Short> listen() {
        byte[] buffer = new byte[INPUT_BLOCK_SIZE];
        LINE.read(buffer, 0, buffer.length);
        return bytesToFrames(buffer);
    }

    /**
     * Temporarily stops the line and flushes the input buffer.
     */
    public void stop() {
        LINE.stop();
        LINE.flush();
    }

    /**
     * Starts the line and flushes the input buffer.
     */
    public void start() {
        LINE.flush();
        LINE.start();
    }

    /**
     * Flushes the input buffer.
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
     * @param buffer Array of frames as bytes to convert
     * @return Vector of frames as signed shorts
     */
    private static Vector<Short> bytesToFrames(byte[] buffer) {
        Vector<Short> out_frames = new Vector<>(buffer.length / 2);
        byte[] frame = new byte[2];
        for(int i = 0; i < buffer.length - 1; i += 2) {
            frame[0] = buffer[i + 1];
            frame[1] = buffer[i];
            out_frames.add(ByteBuffer.wrap(frame).getShort());
        }
        return out_frames;
    }
}
