package org.lavajuno.jfskmodem;

import org.lavajuno.jfskmodem.modem.Receiver;
import org.lavajuno.jfskmodem.modem.Transmitter;

import javax.sound.sampled.*;
import java.nio.charset.StandardCharsets;

public class JFSKModemApplication {
    public static final double TRAINING_TIME = 0.5;

    public static void main(String[] args) {
        /*
        SoundInput s;
        try {
            s = new SoundInput();
            Vector<Short> a = new Vector<>();
            s.start();
            for(int j = 0; j < 50; j++) {
                Vector<Short> b = s.listen();
                a.addAll(b);
            }
            s.close();

            int wrap = 0;
            for(Short i : a) {
                System.out.print(i);
                System.out.print(" ");
                if(wrap > 50) {
                    wrap = 0;
                    System.out.println();
                }
                wrap++;
            }
        } catch(LineUnavailableException e) {
            System.err.println("Failed to initialize line.");
            System.exit(0);
        }
        */


        try {
            Transmitter t = new Transmitter(1200);
            byte[] b = "The quick brown fox jumped over the lazy dog.".getBytes(StandardCharsets.UTF_8);
            t.transmit(b);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        try {
            Receiver r = new Receiver(1200);
            byte[] b = r.receive(100);
            System.out.println(new String(b, StandardCharsets.UTF_8));

        } catch(LineUnavailableException e) {
            throw new RuntimeException(e);
        }

    }
}