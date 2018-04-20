package frames;

/**
 * Flag constants and operations
 *
 * @author Rolv-Arild Braaten
 */
public interface Flags {
    byte ACK = 0x1;
    byte END_STREAM = 0x1;
    byte END_HEADERS = 0x4;
    byte PADDED = 0x8;
    byte PRIORITY = 0x20;

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
