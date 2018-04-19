package frames;

import java.nio.ByteBuffer;

import static frames.ErrorCode.*;
import static frames.FrameType.*;

/**
 * https://http2.github.io/http2-spec/#rfc.section.6.4
 *
 * The RST_STREAM frame (type=0x3) allows for immediate termination of a stream.
 * RST_STREAM is sent to request cancellation of a stream or to indicate that an error condition has occurred.
 * <p>
 * +---------------------------------------------------------------+
 * |                        Error Code (32)                        |
 * +---------------------------------------------------------------+
 * Figure 9: RST_STREAM Frame Payload
 * <p>
 * The RST_STREAM frame contains a single unsigned, 32-bit integer identifying the error code (Section 7).
 * The error code indicates why the stream is being terminated.
 * <p>
 * The RST_STREAM frame does not define any flags.
 * <p>
 * The RST_STREAM frame fully terminates the referenced stream and causes it to enter the "closed" state.
 * After receiving a RST_STREAM on a stream, the receiver MUST NOT send additional frames for that stream,
 * with the exception of PRIORITY.
 * However, after sending the RST_STREAM,
 * the sending endpoint MUST be prepared to receive and process additional frames sent on the stream that might have been sent by the peer prior to the arrival of the RST_STREAM.
 * <p>
 * RST_STREAM frames MUST be associated with a stream.
 * If a RST_STREAM frame is received with a stream identifier of 0x0,
 * the recipient MUST treat this as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * RST_STREAM frames MUST NOT be sent for a stream in the "idle" state.
 * If a RST_STREAM frame identifying an idle stream is received,
 * the recipient MUST treat this as a connection error (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * A RST_STREAM frame with a length other than 4 octets MUST be treated as a connection error (Section 5.4.1) of type FRAME_SIZE_ERROR.
 */
public class RSTStreamFrame extends Frame {

    private final ErrorCode errorCode;

    /**
     * Constructs an RST stream frame
     *
     * @param errorCode An unsigned, 32-bit integer identifying the error code (Section 7).
     *                  The error code indicates why the stream is being terminated.
     * @param streamId  RST_STREAM frames MUST be associated with a stream.
     *                  If a RST_STREAM frame is received with a stream identifier of 0x0,
     *                  the recipient MUST treat this as a connection error
     */
    public RSTStreamFrame(ErrorCode errorCode, int streamId) {
        super(4, RST_STREAM, streamId);
        if (streamId == 0) {
            throw PROTOCOL_ERROR.error();
        }
        this.errorCode = errorCode;
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.putInt(errorCode.code);
        return out;
    }
}
