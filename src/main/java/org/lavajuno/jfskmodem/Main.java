package org.lavajuno.jfskmodem;

import javax.sound.sampled.*;

public class Main {
    public static void main(String[] args) {
        Mixer.Info[] minf = AudioSystem.getMixerInfo();

        for(Mixer.Info i : minf) {
            System.out.println(i.getName());
            System.out.println(i.getDescription());
            System.out.println(i.getVendor());
            System.out.println("----------");
        }
        System.out.println("Hello world!");

        DataLine.Info info = new DataLine.Info(
                SourceDataLine.class,
                new AudioFormat(48000, 16, 1, true, true)
        );

        if(!AudioSystem.isLineSupported(info)) {
            System.err.println("Audio format not supported, quitting now.");
            return;
        }
        SourceDataLine line;
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(new AudioFormat(48000, 16, 1, true, true));
            line.start();
        } catch(LineUnavailableException e) {
            System.err.println("Line unavailable, quitting now.");
            return;
        }
        byte[] a = new byte[5000];
        for(int i = 0; i < 2500; i += 2) {
            int amp = (int) Math.round(Math.floor(Math.sin(i) * 32767));
            String amp_str = Integer.toBinaryString(amp);
            a[i * 2] = Byte.parseByte(amp_str.substring(0, 8));
            a[i * 2 + 1] = Byte.parseByte(amp_str.substring(8, 16));
        }
        line.write()



    }
}