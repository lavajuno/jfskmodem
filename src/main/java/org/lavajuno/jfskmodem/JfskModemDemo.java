package org.lavajuno.jfskmodem;

import org.lavajuno.jfskmodem.log.Log;

import javax.sound.sampled.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class JfskModemDemo {
    public static final double TRAINING_TIME = 0.5;

    private enum Mode {Tx, Rx, None}

    private static Mode mode;
    private static int baud_rate;
    private static Log.Level log_level;

    public static void main(String[] args) {
        // Handle args
        mode = Mode.None;
        baud_rate = 1200;
        log_level = Log.Level.WARN;
        for(int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "-t":
                case "--tx":
                    if(mode == Mode.None) {
                        mode = Mode.Tx;
                    } else {
                        System.err.println("Invalid argument \"" + args[i] + "\": Mode already set.");
                        System.exit(1);
                    }
                    break;

                case "-r":
                case "--rx":
                    if(mode == Mode.None) {
                        mode = Mode.Rx;
                    } else {
                        System.err.println("Invalid argument \"" + args[i] + "\": Mode already set.");
                        System.exit(1);
                    }
                    break;

                case "-b":
                case "--baud":
                    if(i < args.length - 1) {
                        try {
                            baud_rate = Integer.parseInt(args[i+1]);
                            i++;
                        } catch(NumberFormatException e) {
                            System.err.println("Invalid value \"" + args[i+1] + "\" for baud rate.");
                            System.exit(1);
                        }
                    }
                    break;

                case "-v":
                case "--verbose":
                    log_level = Log.Level.DEBUG;
                    break;

                default:
                    System.err.println("Unrecognized argument \"" + args[i] + "\".");
                    System.exit(1);
            }
        }

        switch(mode) {
            case Tx -> doTransmitter(baud_rate, log_level);
            case Rx -> doReceiver(baud_rate, log_level);
            default -> System.err.println("Please specify a mode (--tx or --rx).");
        }
    }

    private static void doTransmitter(int baud_rate, Log.Level log_level) {
        try {
            Scanner in = new Scanner(System.in);
            Transmitter tx = new Transmitter(baud_rate, log_level);
            while(true) {
                System.out.print("Tx~");
                String line = in.nextLine();
                tx.transmit(line.getBytes(StandardCharsets.UTF_8));
            }
        } catch(LineUnavailableException e) {

            throw new RuntimeException(e);
        }
    }

    private static void doReceiver(int baud_rate, Log.Level log_level) {
        try {
            Receiver r = new Receiver(baud_rate, log_level);
            while(true) {
                byte[] b = r.receive(100);
                System.out.println("Rx~" + new String(b, StandardCharsets.UTF_8));
            }
        } catch(LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}