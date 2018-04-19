package frames;

import java.nio.ByteBuffer;

import static frames.ErrorCode.*;
import static frames.FrameType.*;

/**
 * https://http2.github.io/http2-spec/#rfc.section.6.1
 *
 * DATA frames (type=0x0) convey arbitrary, variable-length sequences of octets associated with a stream.
 * One or more DATA frames are used, for instance, to carry HTTP request or response payloads.
 * <p>
 * DATA frames MAY also contain padding. Padding can be added to DATA frames to obscure the size of messages.
 * Padding is a security feature; see Section 10.7.
 * <p>
 * +---------------+
 * |Pad Length? (8)|
 * +---------------+-----------------------------------------------+
 * |                            Data (*)                         ...
 * +---------------------------------------------------------------+
 * |                           Padding (*)                       ...
 * +---------------------------------------------------------------+
 * Figure 6: DATA Frame Payload
 * <p>
 * The DATA frame contains the following fields:
 * <p>
 * Pad Length:
 * An 8-bit field containing the length of the frame padding in units of octets.
 * This field is conditional (as signified by a "?" in the diagram) and is only present if the PADDED flag is set.
 * Data:
 * Application data. The amount of data is the remainder of the frame payload after subtracting the length of the other fields that are present.
 * Padding:
 * Padding octets that contain no application semantic value. Padding octets MUST be set to zero when sending.
 * A receiver is not obligated to verify padding but MAY treat non-zero padding as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * The DATA frame defines the following flags:
 * <p>
 * END_STREAM (0x1):
 * When set, bit 0 indicates that this frame is the last that the endpoint will send for the identified stream.
 * Setting this flag causes the stream to enter one of the "half-closed" states or the "closed" state (Section 5.1).
 * PADDED (0x8):
 * When set, bit 3 indicates that the Pad Length field and any padding that it describes are present.
 * DATA frames MUST be associated with a stream. If a DATA frame is received whose stream identifier field is 0x0,
 * the recipient MUST respond with a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * DATA frames are subject to flow control and can only be sent when a stream is in the "open" or "half-closed (remote)" state.
 * The entire DATA frame payload is included in flow control, including the Pad Length and Padding fields if present.
 * If a DATA frame is received whose stream is not in "open" or "half-closed (local)" state,
 * the recipient MUST respond with a stream error (Section 5.4.2) of type STREAM_CLOSED.
 * <p>
 * The total number of padding octets is determined by the value of the Pad Length field.
 * If the length of the padding is the length of the frame payload or greater,
 * the recipient MUST treat this as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * Note: A frame can be increased in size by one octet by including a Pad Length field with a value of zero.
 */
public class DataFrame extends Frame {

    private static final byte END_STREAM = 0x1;
    private static final byte PADDED = 0x8;

    public byte padLength;

    /**
     * Constructs a data frame.
     *
     * @param streamId  A stream Id expressed as an unsigned 31-bit integer.
     *                  The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param data      The payload of this data frame.
     * @param padLength An 8-bit field containing the length of the frame padding in units of octets.
     *                  The PADDED flag is set if this parameter is not 0.
     * @param endStream When set, bit 0 indicates that this frame is the last that the endpoint will send for the identified stream.
     *                  Setting this flag causes the stream to enter one of the "half-closed" states or the "closed" state.
     */
    public DataFrame(int streamId, ByteBuffer data, byte padLength, boolean endStream) {
        super(DATA, (endStream ? END_STREAM : 0) | ((padLength == 0) ? 0 : PADDED), streamId, data); // TODO add padLength field and pad data before sending it as payload parameter
        if (streamId == 0) {
            throw PROTOCOL_ERROR.error();
        }
        if (padLength > payloadLength()) {
            throw PROTOCOL_ERROR.error();
        }
        // TODO if a DATA frame is received whose stream is not in "open" or "half-closed (local)" state, the recipient MUST respond with a stream error (Section 5.4.2) of type STREAM_CLOSED.
        this.padLength = padLength;
    }

    /**
     * Constructs a data frame with no padding.
     *
     * @param streamId  A stream Id expressed as an unsigned 31-bit integer.
     *                  The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param payload   The payload of this data frame
     * @param endStream When set, bit 0 indicates that this frame is the last that the endpoint will send for the identified stream.
     *                  Setting this flag causes the stream to enter one of the "half-closed" states or the "closed" state.
     */
    public DataFrame(int streamId, ByteBuffer payload, boolean endStream) {
        this(streamId, payload, (byte) 0, endStream);
    }
}
