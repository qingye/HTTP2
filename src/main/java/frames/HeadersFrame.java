package frames;

import java.nio.ByteBuffer;

import static frames.ErrorCode.PROTOCOL_ERROR;
import static frames.Flags.*;
import static frames.FrameType.HEADERS;


/**
 * The HEADERS frame (type=0x1) is used to open a stream (Section 5.1),
 * and additionally carries a header block fragment.  HEADERS frames can
 * be sent on a stream in the "idle", "reserved (local)", "open", or
 * "half-closed (remote)" state.
 * <p>
 * +---------------+
 * |Pad Length? (8)|
 * +-+-------------+-----------------------------------------------+
 * |E|                 Stream Dependency? (31)                     |
 * +-+-------------+-----------------------------------------------+
 * |  Weight? (8)  |
 * +-+-------------+-----------------------------------------------+
 * |                   Header Block Fragment (*)                 ...
 * +---------------------------------------------------------------+
 * |                           Padding (*)                       ...
 * +---------------------------------------------------------------+
 * <p>
 * Figure 7: HEADERS Frame Payload
 * <p>
 * The HEADERS frame payload has the following fields:
 * <p>
 * Pad Length:  An 8-bit field containing the length of the frame
 * padding in units of octets.  This field is only present if the
 * PADDED flag is set.
 * <p>
 * E: A single-bit flag indicating that the stream dependency is
 * exclusive (see Section 5.3).  This field is only present if the
 * PRIORITY flag is set.
 * <p>
 * Stream Dependency:  A 31-bit stream identifier for the stream that
 * this stream depends on (see Section 5.3).  This field is only
 * present if the PRIORITY flag is set.
 * <p>
 * Weight:  An unsigned 8-bit integer representing a priority weight for
 * the stream (see Section 5.3).  Add one to the value to obtain a
 * weight between 1 and 256.  This field is only present if the
 * PRIORITY flag is set.
 * <p>
 * Header Block Fragment:  A header block fragment (Section 4.3).
 * <p>
 * Padding:  Padding octets.
 * <p>
 * The HEADERS frame defines the following flags:
 * <p>
 * END_STREAM (0x1):  When set, bit 0 indicates that the header block
 * (Section 4.3) is the last that the endpoint will send for the
 * identified stream.
 * <p>
 * A HEADERS frame carries the END_STREAM flag that signals the end
 * of a stream.  However, a HEADERS frame with the END_STREAM flag
 * set can be followed by CONTINUATION frames on the same stream.
 * Logically, the CONTINUATION frames are part of the HEADERS frame.
 * <p>
 * END_HEADERS (0x4):  When set, bit 2 indicates that this frame
 * contains an entire header block (Section 4.3) and is not followed
 * by any CONTINUATION frames.
 * <p>
 * A HEADERS frame without the END_HEADERS flag set MUST be followed
 * by a CONTINUATION frame for the same stream.  A receiver MUST
 * treat the receipt of any other type of frame or a frame on a
 * different stream as a connection error (Section 5.4.1) of type
 * PROTOCOL_ERROR.
 * <p>
 * PADDED (0x8):  When set, bit 3 indicates that the Pad Length field
 * and any padding that it describes are present.
 * <p>
 * PRIORITY (0x20):  When set, bit 5 indicates that the Exclusive Flag
 * (E), Stream Dependency, and Weight fields are present; see
 * Section 5.3.
 * <p>
 * The payload of a HEADERS frame contains a header block fragment
 * (Section 4.3).  A header block that does not fit within a HEADERS
 * frame is continued in a CONTINUATION frame (Section 6.10).
 * <p>
 * HEADERS frames MUST be associated with a stream.  If a HEADERS frame
 * is received whose stream identifier field is 0x0, the recipient MUST
 * respond with a connection error (Section 5.4.1) of type
 * PROTOCOL_ERROR.
 * <p>
 * The HEADERS frame changes the connection state as described in
 * Section 4.3.
 * <p>
 * The HEADERS frame can include padding.  Padding fields and flags are
 * identical to those defined for DATA frames (Section 6.1).  Padding
 * that exceeds the size remaining for the header block fragment MUST be
 * treated as a PROTOCOL_ERROR.
 * <p>
 * Prioritization information in a HEADERS frame is logically equivalent
 * to a separate PRIORITY frame, but inclusion in HEADERS avoids the
 * potential for churn in stream prioritization when new streams are
 * created.  Prioritization fields in HEADERS frames subsequent to the
 * first on a stream reprioritize the stream (Section 5.3.3).
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class HeadersFrame extends Frame {

    private int streamDependency = -1;
    private byte padLength;
    private boolean E = false;
    private byte weight = -1;
    private ByteBuffer headerBlockFragment;


    /**
     * Constructor to make a headers frame without the PRIORITY flag being set.
     *
     * @param padLength           An 8-bit field containing the length of the frame padding in units of octets.
     *                            This field is only present if the PADDED flag is set.
     * @param headerBlockFragment A header block fragment.
     * @param endHeaders          When set, bit 2 indicates that this frame contains an entire header block and is not followed by any CONTINUATION frames.
     * @param streamId            A stream Id expressed as an unsigned 31-bit integer.
     *                            The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param endStream           When set, bit 0 indicates that the header block is the last that the endpoint will send for the identified stream.
     */
    public HeadersFrame(boolean endStream, boolean endHeaders, byte padLength, int streamId, ByteBuffer headerBlockFragment) {
        super(6 + headerBlockFragment.position() + padLength, HEADERS, combine((endStream ? END_STREAM : 0), (endHeaders ? END_HEADERS : 0), ((padLength == 0) ? 0 : PADDED)), streamId);
        if (streamId == 0) {
            throw PROTOCOL_ERROR.error();
        }
        if (padLength > payloadLength()) {
            throw PROTOCOL_ERROR.error();
        }
        if (streamDependency < 0) {
            throw new IllegalArgumentException("Invalid stream dependency");
        }
        this.padLength = padLength;
        this.headerBlockFragment = headerBlockFragment;
    }

    /**
     * Constructor to make a headers frame with the PRIORITY flag being set.
     *
     * @param padLength           An 8-bit field containing the length of the frame padding in units of octets.
     *                            This field is only present if the PADDED flag is set.
     * @param E                   A single-bit flag indicating that the stream dependency is exclusive.
     *                            This field is only present if the PRIORITY flag is set.
     * @param streamDependency    A 31-bit stream Id for the stream that this stream depends on.
     *                            This field is only present if the PRIORITY flag is set.
     * @param weight              An unsigned 8-bit integer representing a priority weight for the stream.
     *                            Add one to the value to obtain a weight between 1 and 256. This field is only present if the PRIORITY flag is set.
     * @param streamId            A stream Id expressed as an unsigned 31-bit integer.
     *                            The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param headerBlockFragment The payload of this frame.
     * @param endHeaders          When set, bit 2 indicates that this frame contains an entire header block and is not followed by any CONTINUATION frames.
     * @param endStream           When set, bit 0 indicates that the header block is the last that the endpoint will send for the identified stream.
     */
    public HeadersFrame(byte padLength, boolean E, int streamDependency, byte weight, int streamId, ByteBuffer headerBlockFragment, boolean endHeaders, boolean endStream) {
        this(endStream, endHeaders, padLength, streamId, headerBlockFragment);
        addFlag(PRIORITY);
        this.streamDependency = streamDependency;
        this.padLength = padLength;
        this.E = E;
        this.weight = weight;
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.put(padLength);
        out.putInt(E ? streamDependency & -2147483648 : streamDependency); // -2147483648 is only the first bit
        out.put(weight);
        out.put(headerBlockFragment);
        out.put(ByteBuffer.allocate(padLength));
        return out;
    }
}
