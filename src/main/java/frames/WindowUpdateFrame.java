package frames;

import java.nio.ByteBuffer;

import static frames.ErrorCode.PROTOCOL_ERROR;
import static frames.FrameType.WINDOW_UPDATE;

/**
 * The WINDOW_UPDATE frame (type=0x8) is used to implement flow control;
 * see Section 5.2 for an overview.
 * <p>
 * Flow control operates at two levels: on each individual stream and on
 * the entire connection.
 * <p>
 * Both types of flow control are hop by hop, that is, only between the
 * two endpoints.  Intermediaries do not forward WINDOW_UPDATE frames
 * between dependent connections.  However, throttling of data transfer
 * by any receiver can indirectly cause the propagation of flow-control
 * information toward the original sender.
 * <p>
 * Flow control only applies to frames that are identified as being
 * subject to flow control.  Of the frame types defined in this
 * document, this includes only DATA frames.  Frames that are exempt
 * from flow control MUST be accepted and processed, unless the receiver
 * is unable to assign resources to handling the frame.  A receiver MAY
 * respond with a stream error (Section 5.4.2) or connection error
 * (Section 5.4.1) of type FLOW_CONTROL_ERROR if it is unable to accept
 * a frame.
 * <pre>
 * {@code
 * +-+-------------------------------------------------------------+
 * |R|              Window Size Increment (31)                     |
 * +-+-------------------------------------------------------------+
 * }
 * </pre>
 * Figure 14: WINDOW_UPDATE Payload Format
 * <p>
 * The payload of a WINDOW_UPDATE frame is one reserved bit plus an
 * unsigned 31-bit integer indicating the number of octets that the
 * sender can transmit in addition to the existing flow-control window.
 * The legal range for the increment to the flow-control window is 1 to
 * 2^31-1 (2,147,483,647) octets.
 * <p>
 * The WINDOW_UPDATE frame does not define any flags.
 * <p>
 * The WINDOW_UPDATE frame can be specific to a stream or to the entire
 * connection.  In the former case, the frame's stream identifier
 * indicates the affected stream; in the latter, the value "0" indicates
 * that the entire connection is the subject of the frame.
 * <p>
 * A receiver MUST treat the receipt of a WINDOW_UPDATE frame with an
 * flow-control window increment of 0 as a stream error (Section 5.4.2)
 * of type PROTOCOL_ERROR; errors on the connection flow-control window
 * MUST be treated as a connection error (Section 5.4.1).
 * <p>
 * WINDOW_UPDATE can be sent by a peer that has sent a frame bearing the
 * END_STREAM flag.  This means that a receiver could receive a
 * WINDOW_UPDATE frame on a "half-closed (remote)" or "closed" stream.
 * A receiver MUST NOT treat this as an error (see Section 5.1).
 * <p>
 * A receiver that receives a flow-controlled frame MUST always account
 * for its contribution against the connection flow-control window,
 * unless the receiver treats this as a connection error
 * (Section 5.4.1).  This is necessary even if the frame is in error.
 * The sender counts the frame toward the flow-control window, but if
 * the receiver does not, the flow-control window at the sender and
 * receiver can become different.
 * <p>
 * A WINDOW_UPDATE frame with a length other than 4 octets MUST be
 * treated as a connection error (Section 5.4.1) of type
 * FRAME_SIZE_ERROR.
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class WindowUpdateFrame extends Frame {

    public final int windowSizeIncrement;

    /**
     * Constructs a window update frame
     *
     * @param streamId A stream identifier expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param windowSizeIncrement An unsigned 31-bit integer indicating the number of octets that the sender can transmit in addition to the existing flow-control window.
     *                            The legal range for the increment to the flow-control window is 1 to 231-1 (2,147,483,647) octets.
     */
    public WindowUpdateFrame(int streamId, int windowSizeIncrement) {
        super(streamId, 4, WINDOW_UPDATE);
        if (windowSizeIncrement <= 0) {
            throw PROTOCOL_ERROR.error();
        }
        this.windowSizeIncrement = windowSizeIncrement;
    }

    /**
     * Crates a window update frame with the specified flags, streamId and payload.
     *
     * @param flags the flags of this frame.
     * @param streamId the stream id of this frame.
     * @param payload the payload of this frame.
     */
    public WindowUpdateFrame(byte flags, int streamId, ByteBuffer payload) {
        super(streamId, payload.remaining(), WINDOW_UPDATE, flags);
        this.windowSizeIncrement = payload.getInt() & 2147483647;
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.putInt(windowSizeIncrement);
        return out;
    }

    @Override
    public String toString() {
        return super.toString() + ", windowSizeIncrement=" + windowSizeIncrement;
    }
}
