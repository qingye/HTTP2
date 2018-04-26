package frames;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import static frames.Flags.ACK;
import static frames.FrameType.PING;

/**
 * The PING frame (type=0x6) is a mechanism for measuring a minimal
 * round-trip time from the sender, as well as determining whether an
 * idle connection is still functional.  PING frames can be sent from
 * any endpoint.
 * <pre>
 * {@code
 * +---------------------------------------------------------------+
 * |                                                               |
 * |                      Opaque Data (64)                         |
 * |                                                               |
 * +---------------------------------------------------------------+
 * }
 * </pre>
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
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class PingFrame extends Frame {

    public final ByteBuffer opaqueData;

    /**
     * Constructs a ping frame.
     *
     * @param streamId   A stream identifier expressed as an unsigned 31-bit integer.
     *                   The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param ack        When set, bit 0 indicates that this PING frame is a PING response.
     *                   An endpoint MUST set this flag in PING responses. An endpoint MUST NOT respond to PING frames containing this flag.
     * @param opaqueData The data of this ping frame.
     */
    public PingFrame(int streamId, boolean ack, ByteBuffer opaqueData) {
        super(streamId, 8, PING, ack ? ACK : 0);
        this.opaqueData = opaqueData;
    }

    /**
     * Creates a ping frame with random data.
     *
     * @param streamId A stream identifier expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     */
    public PingFrame(int streamId) {
        this(streamId, false, randomData());
    }

    private static ByteBuffer randomData() {
        byte[] bytes = new byte[8];
        new Random().nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }

    /**
     * Crates a ping frame with the specified flags, streamId and payload.
     *
     * @param flags    the flags of this frame.
     * @param streamId the stream id of this frame.
     * @param payload  the payload of this frame.
     */
    public PingFrame(byte flags, int streamId, ByteBuffer payload) {
        super(streamId, payload.remaining(), PING, flags);
        this.opaqueData = payload.rewind();
    }


    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.put(opaqueData);
        return out.flip();
    }

    @Override
    public String toString() {
        opaqueData.rewind();
        byte[] bytes = new byte[opaqueData.remaining()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = opaqueData.get();
        }
        opaqueData.rewind();
        return super.toString() + ", opaqueData={" + Arrays.toString(bytes) + "}";
    }
}
