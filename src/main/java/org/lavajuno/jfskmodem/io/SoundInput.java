package org.lavajuno.jfskmodem.io;

import javax.sound.sampled.*;
import java.util.Vector;

public class SoundInput {
    private static final int INPUT_BLOCK_SIZE = 4096;
    private final TargetDataLine LINE;

    public SoundInput() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        LINE = (TargetDataLine) AudioSystem.getLine(info);
        LINE.open(format);
        LINE.start();
    }

    public Vector<Byte> listen() {
        byte[] buffer = new byte[INPUT_BLOCK_SIZE];
        LINE.read(buffer, 0, buffer.length);
        return convertBuffer(buffer);
    }

    public void pause() {
        LINE.stop();
        LINE.flush();
    }

    public void resume() {
        LINE.flush();
        LINE.start();
    }

    public void close() {
        LINE.stop();
        LINE.close();
    }
    
    private static Vector<Byte> convertBuffer(byte[] buffer) {
        Vector<Byte> output = new Vector<>(buffer.length);
        for(byte i : buffer) { output.add(i); }
        return output;
    }


}
