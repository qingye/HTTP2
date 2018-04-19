package frames;

/**
 * Flag constants and operations
 *
 * @author Rolv-Arild Braaten
 */
class Flags {

    static final byte ACK = 0x1;
    static final byte END_STREAM = 0x1;
    static final byte END_HEADERS = 0x4;
    static final byte PADDED = 0x8;
    static final byte PRIORITY = 0x20;


    private Flags() {
    }

    static byte combine(byte... flags) {
        int out = 0;
        for (byte flag : flags) {
            out |= flag;
        }
        return (byte) (out & 0xff);
    }

    static byte separate(byte... flags) {
        int out = 0xff;
        for (byte flag : flags) {
            out &= flag;
        }
        return (byte) (out & 0xff);
    }

    static boolean isSet(byte flags, byte flag) {
        return (flags & flag) != 0;
    }
}
