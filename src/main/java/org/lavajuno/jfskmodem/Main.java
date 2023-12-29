package org.lavajuno.jfskmodem;

import org.lavajuno.jfskmodem.ecc.Hamming;
import org.lavajuno.jfskmodem.io.SoundInput;

import javax.sound.sampled.*;
import java.util.Arrays;
import java.util.Vector;

public class Main {
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
        System.out.println(Arrays.toString(Hamming.encode(new byte[]{1, 0, 1, 1})));
        System.out.println(Arrays.toString(Hamming.decode(new byte[]{0, 1, 1, 0, 1, 1, 1})));
    }
}