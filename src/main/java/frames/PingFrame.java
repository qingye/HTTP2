package frames;

import java.nio.ByteBuffer;
import java.util.Random;

import static frames.ErrorCode.*;
import static frames.Flags.ACK;
import static frames.FrameType.*;

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
