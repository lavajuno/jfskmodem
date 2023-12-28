package org.lavajuno.jfskmodem.io;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class SoundInput {
    public SoundInput() {
    }

    AudioFormat getDefaultAudioFormat() {
        return new AudioFormat(48000, 16, 1, true, true);
    }
}
