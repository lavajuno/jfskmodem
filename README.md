# jfskmodem

A software-defined Audio Frequency-Shift Keying modem designed for analog FM radios.
Uses the device's default audio input and output.

[Source Code](https://github.com/lavajuno/jfskmodem)

[Releases](https://github.com/lavajuno/jfskmodem/releases)

[Documentation](https://lavajuno.github.io/jfskmodem/docs/index.html)

jfskmodem is a re-implementation of [afskmodem](https://github.com/lavajuno/afskmodem)
in Java, with many improvements to its structure and efficiency.

## Usage Example
Transmitting a message:

```java
import org.lavajuno.jfskmodem.Transmitter;

import java.nio.charset.StandardCharsets;

public static void main(String[] args) {
    Transmitter t = new Transmitter(1200);
    String s = "Hello world!";
    t.transmit(s.getBytes(StandardCharsets.UTF_8));
}
```

Receiving a message:

```java
import org.lavajuno.jfskmodem.Receiver;

import java.nio.charset.StandardCharsets;

public static void main(String[] args) {
    Receiver r = new Receiver(1200);
    byte[] b = r.receive(100);
    System.out.println(new String(b, StandardCharsets.UTF_8));
}
```

## Licensing

jfskmodem is Free and Open Source Software, and is released under the MIT license. (See [`LICENSE`](LICENSE))
