package org.lavajuno.jfskmodem.main;

import org.lavajuno.jfskmodem.modem.Transmitter;

import javax.sound.sampled.*;

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
            Transmitter t = new Transmitter(6000);
            byte[] b = "BBBBBBBBBBBBBBBBBBBBBB:DSKJFH:SGHGBJLKWEJBFUIWBGJdfhsdfhglkjsbglkebroiugwbgpwjthpkwbejigfuebhyrubtglwkuebtoi4btueBBBBBBBBBBBBBBBBBBBBBBBB".getBytes();
            t.transmit(b);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

    }
}