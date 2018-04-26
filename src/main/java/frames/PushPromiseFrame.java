package frames;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static frames.Flags.*;
import static frames.FrameType.PUSH_PROMISE;

/**
 * The PUSH_PROMISE frame (type=0x5) is used to notify the peer endpoint
 * in advance of streams the sender intends to initiate.  The
 * PUSH_PROMISE frame includes the unsigned 31-bit identifier of the
 * stream the endpoint plans to create along with a set of headers that
 * provide additional context for the stream.  Section 8.2 contains a
 * thorough description of the use of PUSH_PROMISE frames.
 * <pre>
 * {@code
 * +---------------+
 * |Pad Length? (8)|
 * +-+-------------+-----------------------------------------------+
 * |R|                  Promised Stream ID (31)            |
 * +-+-----------------------------+-------------------------------+
 * |                   Header Block Fragment (*)                 ...
 * +---------------------------------------------------------------+
 * |                           Padding (*)                       ...
 * +---------------------------------------------------------------+
 * }
 * </pre>
 * Figure 11: PUSH_PROMISE Payload Format
 * <p>
 * The PUSH_PROMISE frame payload has the following fields:
 * <p>
 * Pad Length:  An 8-bit field containing the length of the frame
 * padding in units of octets.  This field is only present if the
 * PADDED flag is set.
 * <p>
 * R: A single reserved bit.
 * <p>
 * Promised Stream ID:  An unsigned 31-bit integer that identifies the
 * stream that is reserved by the PUSH_PROMISE.  The promised stream
 * identifier MUST be a valid choice for the next stream sent by the
 * sender (see "new stream identifier" in Section 5.1.1).
 * <p>
 * Header Block Fragment:  A header block fragment (Section 4.3)
 * containing request header fields.
 * <p>
 * Padding:  Padding octets.
 * The PUSH_PROMISE frame defines the following flags:
 * <p>
 * END_HEADERS (0x4):  When set, bit 2 indicates that this frame
 * contains an entire header block (Section 4.3) and is not followed
 * by any CONTINUATION frames.
 * <p>
 * A PUSH_PROMISE frame without the END_HEADERS flag set MUST be
 * followed by a CONTINUATION frame for the same stream.  A receiver
 * MUST treat the receipt of any other type of frame or a frame on a
 * different stream as a connection error (Section 5.4.1) of type
 * PROTOCOL_ERROR.
 * <p>
 * PADDED (0x8):  When set, bit 3 indicates that the Pad Length field
 * and any padding that it describes are present.
 * <p>
 * PUSH_PROMISE frames MUST only be sent on a peer-initiated stream that
 * is in either the "open" or "half-closed (remote)" state.  The stream
 * identifier of a PUSH_PROMISE frame indicates the stream it is
 * associated with.  If the stream identifier field specifies the value
 * 0x0, a recipient MUST respond with a connection error (Section 5.4.1)
 * of type PROTOCOL_ERROR.
 * <p>
 * Promised streams are not required to be used in the order they are
 * promised.  The PUSH_PROMISE only reserves stream identifiers for
 * later use.
 * <p>
 * PUSH_PROMISE MUST NOT be sent if the SETTINGS_ENABLE_PUSH setting of
 * the peer endpoint is set to 0.  An endpoint that has set this setting
 * and has received acknowledgement MUST treat the receipt of a
 * PUSH_PROMISE frame as a connection error (Section 5.4.1) of type
 * PROTOCOL_ERROR.
 * <p>
 * Recipients of PUSH_PROMISE frames can choose to reject promised
 * streams by returning a RST_STREAM referencing the promised stream
 * identifier back to the sender of the PUSH_PROMISE.
 * <p>
 * A PUSH_PROMISE frame modifies the connection state in two ways.
 * First, the inclusion of a header block (Section 4.3) potentially
 * modifies the state maintained for header compression.  Second,
 * PUSH_PROMISE also reserves a stream for later use, causing the
 * promised stream to enter the "reserved" state.  A sender MUST NOT
 * send a PUSH_PROMISE on a stream unless that stream is either "open"
 * or "half-closed (remote)"; the sender MUST ensure that the promised
 * stream is a valid choice for a new stream identifier (Section 5.1.1)
 * (that is, the promised stream MUST be in the "idle" state).
 * <p>
 * Since PUSH_PROMISE reserves a stream, ignoring a PUSH_PROMISE frame
 * causes the stream state to become indeterminate.  A receiver MUST
 * treat the receipt of a PUSH_PROMISE on a stream that is neither
 * "open" nor "half-closed (local)" as a connection error
 * (Section 5.4.1) of type PROTOCOL_ERROR.  However, an endpoint that
 * has sent RST_STREAM on the associated stream MUST handle PUSH_PROMISE
 * frames that might have been created before the RST_STREAM frame is
 * received and processed.
 * <p>
 * A receiver MUST treat the receipt of a PUSH_PROMISE that promises an
 * illegal stream identifier (Section 5.1.1) as a connection error
 * (Section 5.4.1) of type PROTOCOL_ERROR.  Note that an illegal stream
 * identifier is an identifier for a stream that is not currently in the
 * "idle" state.
 * <p>
 * The PUSH_PROMISE frame can include padding.  Padding fields and flags
 * are identical to those defined for DATA frames (Section 6.1).
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class PushPromiseFrame extends Frame {

    public final short padLength;
    public final int promisedStreamId;
    public final ByteBuffer headerBlockFragment;

    /**
     * Constructs a push promise frame
     *
     * @param padLength           An 8-bit field containing the length of the frame padding in units of octets.
     *                            This field is only present if the PADDED flag is set.
     * @param promisedStreamID    An unsigned 31-bit integer that identifies the stream that is reserved by the PUSH_PROMISE.
     *                            The promised stream identifier MUST be a valid choice for the next stream sent by the sender.
     * @param headerBlockFragment A header block fragment containing request header fields.
     * @param endHeaders          When set, bit 2 indicates that this frame contains an entire header block and is not followed by any CONTINUATION frames.
     */
    public PushPromiseFrame(int streamId, short padLength, int promisedStreamID, ByteBuffer headerBlockFragment, boolean endHeaders) {
        super(streamId, ((padLength > 0) ? 1 : 0) + 4 + headerBlockFragment.remaining() + padLength, PUSH_PROMISE, combine((padLength != 0) ? PADDED : 0, (endHeaders ? END_HEADERS : 0)));
        this.padLength = (short) (padLength & 0xff);
        this.promisedStreamId = promisedStreamID;
        this.headerBlockFragment = headerBlockFragment;
        // TODO ensure SETTINGS_ENABLE_PUSH is not disabled when sending
    }

    /**
     * Crates a push promise frame with the specified flags, streamId and payload.
     *
     * @param flags    the flags of this frame.
     * @param streamId the stream id of this frame.
     * @param payload  the payload of this frame.
     */
    public PushPromiseFrame(byte flags, int streamId, ByteBuffer payload) {
        super(streamId, payload.remaining(), PUSH_PROMISE, flags);
        this.padLength = (short) (payload.get() & 0xff);
        this.promisedStreamId = payload.getInt() & 2147483647;
        ByteBuffer slice = payload.slice();
        slice.limit(slice.limit() - padLength);
        this.headerBlockFragment = slice;

    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.put((byte) (padLength & 0xff));
        out.putInt(promisedStreamId);
        out.put(headerBlockFragment);
        out.put(ByteBuffer.allocate(padLength));
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
        return super.toString() + ", padLength=" + padLength + ", promisedStreamId=" + promisedStreamId + ", headerBlockFragment={" + Arrays.toString(bytes) + "}";
    }
}
