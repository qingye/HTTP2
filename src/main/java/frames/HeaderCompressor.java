package frames;

import com.twitter.hpack.Decoder;
import com.twitter.hpack.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Compresses and decompresses headers.
 */
public class HeaderCompressor {

    private HeaderCompressor() {
    }

    /**
     * Compresses a byte buffer with HPACK.
     *
     * @param bb the byte buffer to compress.
     * @return a compressed version of the byte buffer.
     */
    public static ByteBuffer compress(ByteBuffer bb) {
        bb.rewind();
        Encoder encoder = new Encoder(4096);
        byte[] bytes = new byte[bb.remaining()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = bb.get();
        }
        String string = new String(bytes).replaceAll(" ", "");
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
        return ByteBuffer.wrap(os.toByteArray());
    }

    /**
     * Decompresses a byte array.
     *
     * @param b the byte array to decompress.
     * @return the decompressed string.
     */
    public static String decompress(byte[] b) {
        final String[] s = {""};
        Decoder decoder = new Decoder(4096, 4096);
        ByteArrayInputStream baos = new ByteArrayInputStream(b);
        try {
            decoder.decode(
                    baos,
                    (name, value, sensitive)
                            -> s[0] += new String(name) + ": " + new String(value) + "\r\n"
            );
            decoder.endHeaderBlock();
            baos.close();
        } catch (IOException e) {
            System.err.println("Error decoding header");
        }
        return s[0];
    }
}
