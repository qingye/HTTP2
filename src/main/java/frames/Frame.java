package frames;

import java.nio.ByteBuffer;

/**
 * https://http2.github.io/http2-spec/#rfc.section.4.1
 * <p>
 * All frames begin with a fixed 9-octet header followed by a variable-length payload.
 * <p>
 * +-----------------------------------------------+
 * |                 Length (24)                   |
 * +---------------+---------------+---------------+
 * |   Type (8)    |   Flags (8)   |
 * +-+-------------+---------------+-------------------------------+
 * |R|                 Stream Identifier (31)                      |
 * +=+=============================================================+
 * |                   Frame Payload (0...)                      ...
 * +---------------------------------------------------------------+
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
 * Flags:
 * An 8-bit field reserved for boolean flags specific to the frame type.
 * <p>
 * Flags are assigned semantics specific to the indicated frame type.
 * Flags that have no defined semantics for a particular frame type MUST be ignored and MUST be left unset (0x0) when sending.
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
 */
abstract class Frame {

    Header header; // contains the header info
    private ByteBuffer payload; // contains the payload

    /**
     * Constructs a frame
     *
     * @param type     The 8-bit type of the frame.
     *                 The frame type determines the format and semantics of the frame.
     *                 Implementations MUST ignore and discard any frame that has a type that is unknown.
     * @param flags    An 8-bit field reserved for boolean flags specific to the frame type.
     *                 Flags are assigned semantics specific to the indicated frame type.
     *                 Flags that have no defined semantics for a particular frame type MUST be ignored and MUST be left unset (0x0) when sending.
     * @param streamId A stream Id expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param payload  The payload of this frame. The structure and content of the frame payload is dependent entirely on the frame type.
     */
    Frame(FrameType type, int flags, int streamId, ByteBuffer payload) {
        this.header = new Header(((payload == null) ? 0 : payload.position()), type, (byte) flags, streamId);
        this.payload = payload;
    }

    int payloadLength() {
        return header.length;
    }

    FrameType getType() {
        return header.type;
    }

    void setStreamId(int streamId) {
        header.streamId = streamId;
    }

    void addFlag(byte flag) {
        header.flags |= flag;
    }

    void removeFlag(byte flag) {
        header.flags &= flag ^ 0xff;
    }
}
