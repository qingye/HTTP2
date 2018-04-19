package frames;

import java.nio.ByteBuffer;
import java.util.Random;

import static frames.ErrorCode.PROTOCOL_ERROR;
import static frames.Flags.ACK;
import static frames.FrameType.PING;

/**
 * The PING frame (type=0x6) is a mechanism for measuring a minimal
 * round-trip time from the sender, as well as determining whether an
 * idle connection is still functional.  PING frames can be sent from
 * any endpoint.
 * <p>
 * +---------------------------------------------------------------+
 * |                                                               |
 * |                      Opaque Data (64)                         |
 * |                                                               |
 * +---------------------------------------------------------------+
 * <p>
 * Figure 12: PING Payload Format
 * <p>
 * In addition to the frame header, PING frames MUST contain 8 octets of
 * opaque data in the payload.  A sender can include any value it
 * chooses and use those octets in any fashion.
 * <p>
 * Receivers of a PING frame that does not include an ACK flag MUST send
 * a PING frame with the ACK flag set in response, with an identical
 * payload.  PING responses SHOULD be given higher priority than any
 * other frame.
 * <p>
 * The PING frame defines the following flags:
 * <p>
 * ACK (0x1):  When set, bit 0 indicates that this PING frame is a PING
 * response.  An endpoint MUST set this flag in PING responses.  An
 * endpoint MUST NOT respond to PING frames containing this flag.
 * <p>
 * PING frames are not associated with any individual stream.  If a PING
 * frame is received with a stream identifier field value other than
 * 0x0, the recipient MUST respond with a connection error
 * (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * Receipt of a PING frame with a length field value other than 8 MUST
 * be treated as a connection error (Section 5.4.1) of type
 * FRAME_SIZE_ERROR.
 */
public class PingFrame extends Frame {

    /**
     * Constructs a ping frame
     *
     * @param ack      When set, bit 0 indicates that this PING frame is a PING response.
     *                 An endpoint MUST set this flag in PING responses. An endpoint MUST NOT respond to PING frames containing this flag.
     * @param streamId A stream Id expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     */
    PingFrame(boolean ack, int streamId) {
        super(8, PING, ack ? ACK : 0, streamId);
        if (streamId == 0) {
            throw PROTOCOL_ERROR.error();
        }
        // TODO check that length of payload is 8 bytes
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        byte[] randomBytes = new byte[8];
        new Random().nextBytes(randomBytes);
        out.put(randomBytes);
        return out;
    }
}