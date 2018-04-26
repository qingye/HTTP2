package frames;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static frames.ErrorCode.PROTOCOL_ERROR;
import static frames.Flags.*;
import static frames.FrameType.DATA;

/**
 * DATA frames (type=0x0) convey arbitrary, variable-length sequences of
 * octets associated with a stream.  One or more DATA frames are used,
 * for instance, to carry HTTP request or response payloads.
 * <p>
 * DATA frames MAY also contain padding.  Padding can be added to DATA
 * frames to obscure the size of messages.  Padding is a security
 * feature; see Section 10.7.
 * <pre>
 * {@code
 * +---------------+
 * |Pad Length? (8)|
 * +---------------+-----------------------------------------------+
 * |                            Data (*)                         ...
 * +---------------------------------------------------------------+
 * |                           Padding (*)                       ...
 * +---------------------------------------------------------------+
 * }
 * </pre>
 * Figure 6: DATA Frame Payload
 * <p>
 * The DATA frame contains the following fields:
 * <p>
 * Pad Length:  An 8-bit field containing the length of the frame
 * padding in units of octets.  This field is conditional (as
 * signified by a "?" in the diagram) and is only present if the
 * PADDED flag is set.
 * <p>
 * Data:  Application data.  The amount of data is the remainder of the
 * frame payload after subtracting the length of the other fields
 * that are present.
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class DataFrame extends Frame {

    public short padLength;
    private ByteBuffer data;

    /**
     * Constructs a data frame.
     *
     * @param data      The payload of this data frame.
     * @param padLength An 8-bit field containing the length of the frame padding in units of octets.
     *                  The PADDED flag is set if this parameter is not 0.
     * @param endStream When set, bit 0 indicates that this frame is the last that the endpoint will send for the identified stream.
     *                  Setting this flag causes the stream to enter one of the "half-closed" states or the "closed" state.
     */
    public DataFrame(int streamId, ByteBuffer data, short padLength, boolean endStream) {
        super(streamId, ((padLength == 0) ? 1 : 0) + data.remaining() + padLength, DATA, combine((endStream ? END_STREAM : 0), (padLength == 0) ? 0 : PADDED));
        if (padLength > length) {
            throw PROTOCOL_ERROR.error();
        }
        this.data = data;
        // TODO if a DATA frame is received whose stream is not in "open" or "half-closed (local)" state, the recipient MUST respond with a stream error (Section 5.4.2) of type STREAM_CLOSED.
        this.padLength = (short) (padLength & 0xff);
    }

    /**
     * Constructs a data frame with no padding.
     *
     * @param data      The payload of this data frame
     * @param endStream When set, bit 0 indicates that this frame is the last that the endpoint will send for the identified stream.
     *                  Setting this flag causes the stream to enter one of the "half-closed" states or the "closed" state.
     */
    public DataFrame(int streamId, ByteBuffer data, boolean endStream) {
        this(streamId, data, (short) 0, endStream);
    }

    /**
     * Crates a data frame with the specified flags, streamId and payload.
     *
     * @param flags the flags of this frame.
     * @param streamId the stream id of this frame.
     * @param payload the payload of this frame.
     */
    public DataFrame(byte flags, int streamId, ByteBuffer payload) {
        super(streamId, payload.remaining(), DATA, flags);
        if (isSet(flags, PADDED)) {
            this.padLength = payload.get();
        } else {
            this.padLength = 0;
        }
        ByteBuffer slice = payload.slice();
        slice.limit(slice.limit() - padLength);
        this.data = slice;
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.put((byte) padLength);
        out.put(data.rewind());
        out.put(ByteBuffer.allocate(padLength));
        return out.flip();
    }

    @Override
    public String toString() {
        data.rewind();
        byte[] bytes = new byte[data.remaining()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = data.get();
        }
        data.rewind();
        return super.toString() + ", padLength=" + padLength + ", data={" + new String(bytes).replaceAll("\\n", "\\\\n").replaceAll("\\r", "\\\\r") + "}";
    }
}
