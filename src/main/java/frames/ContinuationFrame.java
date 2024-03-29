package frames;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static frames.Flags.END_HEADERS;
import static frames.FrameType.CONTINUATION;
import static frames.HeaderCompressor.compress;

/**
 * The CONTINUATION frame (type=0x9) is used to continue a sequence of
 * header block fragments (Section 4.3).  Any number of CONTINUATION
 * frames can be sent, as long as the preceding frame is on the same
 * stream and is a HEADERS, PUSH_PROMISE, or CONTINUATION frame without
 * the END_HEADERS flag set.
 * <pre>
 * {@code
 * +---------------------------------------------------------------+
 * |                   Header Block Fragment (*)                 ...
 * +---------------------------------------------------------------+
 * }
 * </pre>
 * Figure 15: CONTINUATION Frame Payload
 * <p>
 * The CONTINUATION frame payload contains a header block fragment
 * (Section 4.3).
 * <p>
 * The CONTINUATION frame defines the following flag:
 * <p>
 * END_HEADERS (0x4):  When set, bit 2 indicates that this frame ends a
 * header block (Section 4.3).
 * <p>
 * If the END_HEADERS bit is not set, this frame MUST be followed by
 * another CONTINUATION frame.  A receiver MUST treat the receipt of
 * any other type of frame or a frame on a different stream as a
 * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * The CONTINUATION frame changes the connection state as defined in
 * Section 4.3.
 * <p>
 * CONTINUATION frames MUST be associated with a stream.  If a
 * CONTINUATION frame is received whose stream identifier field is 0x0,
 * the recipient MUST respond with a connection error (Section 5.4.1) of
 * type PROTOCOL_ERROR.
 * <p>
 * A CONTINUATION frame MUST be preceded by a HEADERS, PUSH_PROMISE or
 * CONTINUATION frame without the END_HEADERS flag set.  A recipient
 * that observes violation of this rule MUST respond with a connection
 * error (Section 5.4.1) of type PROTOCOL_ERROR.
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class ContinuationFrame extends Frame {

    private ByteBuffer headerBlockFragment;

    /**
     * Constructs a continuation frame.
     *
     * @param streamId            A stream identifier expressed as an unsigned 31-bit integer.
     *                            The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param endHeaders          When set, bit 2 indicates that this frame ends a header block.
     *                            If the END_HEADERS bit is not set, this frame MUST be followed by another CONTINUATION frame.
     *                            A receiver MUST treat the receipt of any other type of frame or a frame on a different
     *                            stream as a connection error of type PROTOCOL_ERROR.
     * @param headerBlockFragment A header block fragment.
     */
    public ContinuationFrame(int streamId, boolean endHeaders, ByteBuffer headerBlockFragment) {
        super(streamId, headerBlockFragment.remaining(), CONTINUATION, endHeaders ? END_HEADERS : 0);
        this.headerBlockFragment = headerBlockFragment;
    }

    /**
     * Crates a continuation frame with the specified flags, streamId and payload.
     *
     * @param flags    the flags of this frame.
     * @param streamId the stream id of this frame.
     * @param payload  the payload of this frame.
     */
    public ContinuationFrame(byte flags, int streamId, ByteBuffer payload) {
        super(streamId, payload.remaining(), CONTINUATION, flags);
        this.headerBlockFragment = payload.slice();
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.put(compress(headerBlockFragment));
        return out.flip();
    }

    @Override
    public String toString() {
        headerBlockFragment.rewind();
        byte[] bytes = new byte[headerBlockFragment.remaining()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = headerBlockFragment.get();
        }
        headerBlockFragment.rewind();
        return super.toString() + ", headerBlockFragment={" + Arrays.toString(bytes) + "}";
    }
}
