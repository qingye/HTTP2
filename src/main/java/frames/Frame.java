package frames;

import java.nio.ByteBuffer;

/**
 * All frames begin with a fixed 9-octet header followed by a variable-length payload.
 *
 * <pre>
 * {@code
 * +-----------------------------------------------+
 * |                 Length (24)                   |
 * +---------------+---------------+---------------+
 * |   Type (8)    |   Flag (8)   |
 * +-+-------------+---------------+-------------------------------+
 * |R|                 Stream Identifier (31)              |
 * +=+=============================================================+
 * |                   Frame Payload (0...)                      ...
 * +---------------------------------------------------------------+
 * }
 * </pre>
 * Figure 1: Frame Layout
 * <p>
 * The fields of the frame header are defined as:
 * <p>
 * Length:
 * The length of the frame payload expressed as an unsigned 24-bit integer.
 * Values greater than 214 (16,384) MUST NOT be sent unless the receiver has set a larger value for SETTINGS_MAX_FRAME_SIZE.
 * <p>
 * The 9 octets of the frame header are not included in this value.
 * <p>
 * Type:
 * The 8-bit type of the frame. The frame type determines the format and semantics of the frame.
 * Implementations MUST ignore and discard any frame that has a type that is unknown.
 * <p>
 * Flag:
 * An 8-bit field reserved for boolean flags specific to the frame type.
 * <p>
 * Flag are assigned semantics specific to the indicated frame type.
 * Flag that have no defined semantics for a particular frame type MUST be ignored and MUST be left unset (0x0) when sending.
 * <p>
 * R:
 * A reserved 1-bit field.
 * The semantics of this bit are undefined,
 * and the bit MUST remain unset (0x0) when sending and MUST be ignored when receiving.
 * <p>
 * Stream Identifier:
 * A stream identifier (see Section 5.1.1) expressed as an unsigned 31-bit integer.
 * The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
 * <p>
 * The structure and content of the frame payload is dependent entirely on the frame type.
 *
 * @author Rolv-Arild Braaten
 */
public abstract class Frame {

    public int length;
    public final byte flags;
    public final FrameType type;
    public int streamId;

    /**
     * Constructs a frame header.
     *
     * @param streamId A stream identifier (see Section 5.1.1) expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param length   The length of the frame payload expressed as an unsigned 24-bit integer.
     *                 Values greater than 214 (16,384) MUST NOT be sent unless the receiver has set a larger value for SETTINGS_MAX_FRAME_SIZE.
     * @param type     The 8-bit type of the frame.
     *                 The frame type determines the format and semantics of the frame. Implementations MUST ignore and discard any frame that has a type that is unknown.
     * @param flags    An 8-bit field reserved for boolean flags specific to the frame type.
     *                 Flags are assigned semantics specific to the indicated frame type.
     *                 Flags that have no defined semantics for a particular frame type MUST be ignored and MUST be left unset (0x0) when sending.
     */
    public Frame(int streamId, int length, FrameType type, byte flags) {
        // TODO check for errors maybe?
        this.length = length;
        this.type = type;
        this.flags = flags;
        this.streamId = streamId;
    }

    /**
     * Creates a frame header with no tags.
     *
     * @param streamId A stream identifier expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param length   The length of the frame payload expressed as an unsigned 24-bit integer.
     *                 Values greater than 214 (16,384) MUST NOT be sent unless the receiver has set a larger value for SETTINGS_MAX_FRAME_SIZE.
     * @param type     The 8-bit type of the frame.
     *                 The frame type determines the format and semantics of the frame. Implementations MUST ignore and discard any frame that has a type that is unknown.
     */
    public Frame(int streamId, int length, FrameType type) {
        this(streamId, length, type, (byte) 0);
    }

    /**
     * @return A ByteBuffer containing the payload of this frame.
     */
    public abstract ByteBuffer payload();

    /**
     * @return A ByteBuffer containing all the information of this frame.
     */
    public ByteBuffer bytes() {
        ByteBuffer out = ByteBuffer.allocate(9 + length);
        out.putShort((short) (length >>> 8));
        out.put((byte) (length & 0xff));
        out.put(type.code);
        out.put(flags);
        out.putInt(streamId & Integer.MAX_VALUE);
        out.put(payload());
        return out.flip();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": length=" + length + ", flags=0b" + Integer.toBinaryString(flags) + ", streamId=" + streamId;
    }
}
