package frames;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Compressor {

    private Compressor() {
    }

    public static ByteBuffer compress(ByteBuffer bb) {
        bb.rewind();
        Encoder encoder = new Encoder(4096);
        byte[] bytes = new byte[bb.remaining()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bb.get();
        }
        String string = new String(bytes);
        String[] split = string.split("[\\n\\r]+");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            for (String s1 : split) {
                String[] s1Split = s1.split(":", 3);
                if (s1Split.length == 3)
                    encoder.encodeHeader(os, (":" + s1Split[1]).getBytes(), s1Split[2].getBytes(), false);
                else if (s1Split.length == 2)
                    encoder.encodeHeader(os, s1Split[0].getBytes(), s1Split[1].getBytes(), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        StringBuilder s = new StringBuilder();
//        for (byte b : os.toByteArray()) {
//            StringBuilder bin = new StringBuilder(Integer.toBinaryString(b & 0xff));
//            for (int i = bin.length(); i < 8; i++) {
//                bin.insert(0, "0");
//            }
//            s.append(bin).append(" ");
//        }
//        System.out.println(s);
        return ByteBuffer.wrap(os.toByteArray());
    }

    public static String decompress(byte[] b) {
        final String[] s = {""};
        Decoder decoder = new Decoder(4096, 4096);
        try {
            decoder.decode(
                    new ByteArrayInputStream(b),
                    (name, value, sensitive)
                            -> s[0] += new String(name) + ": " + new String(value) + "\r\n"
            );
            decoder.endHeaderBlock();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s[0];
    }
}
