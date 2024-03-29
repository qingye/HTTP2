package frames;

import java.nio.ByteBuffer;

import static frames.FrameType.PRIORITY;

/**
 * The PRIORITY frame (type=0x2) specifies the sender-advised priority
 * of a stream (Section 5.3).  It can be sent in any stream state,
 * including idle or closed streams.
 * <pre>
 * {@code
 * +-+-------------------------------------------------------------+
 * |E|                  Stream Dependency (31)             |
 * +-+-------------+-----------------------------------------------+
 * |   Weight (8)  |
 * +-+-------------+
 * }
 * </pre>
 * Figure 8: PRIORITY Frame Payload
 * <p>
 * The payload of a PRIORITY frame contains the following fields:
 * <p>
 * E: A single-bit flag indicating that the stream dependency is
 * exclusive (see Section 5.3).
 * <p>
 * Stream Dependency:  A 31-bit stream identifier for the stream that
 * this stream depends on (see Section 5.3).
 * <p>
 * Weight:  An unsigned 8-bit integer representing a priority weight for
 * the stream (see Section 5.3).  Add one to the value to obtain a
 * weight between 1 and 256.
 * <p>
 * The PRIORITY frame does not define any flags.
 * <p>
 * The PRIORITY frame always identifies a stream.  If a PRIORITY frame
 * is received with a stream identifier of 0x0, the recipient MUST
 * respond with a connection error (Section 5.4.1) of type
 * PROTOCOL_ERROR.
 * <p>
 * The PRIORITY frame can be sent on a stream in any state, though it
 * cannot be sent between consecutive frames that comprise a single
 * header block (Section 4.3).  Note that this frame could arrive after
 * processing or frame sending has completed, which would cause it to
 * have no effect on the identified stream.  For a stream that is in the
 * "half-closed (remote)" or "closed" state, this frame can only affect
 * processing of the identified stream and its dependent streams; it
 * does not affect frame transmission on that stream.
 * <p>
 * The PRIORITY frame can be sent for a stream in the "idle" or "closed"
 * state.  This allows for the reprioritization of a group of dependent
 * streams by altering the priority of an unused or closed parent
 * stream.
 * <p>
 * A PRIORITY frame with a length other than 5 octets MUST be treated as
 * a stream error (Section 5.4.2) of type FRAME_SIZE_ERROR.
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class PriorityFrame extends Frame {


    public final boolean E;
    public final int streamDependency;
    public final short weight;

    /**
     * Constructs a priority frame
     *
     * @param streamId         A stream identifier expressed as an unsigned 31-bit integer.
     *                         The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     * @param E                A single-bit flag indicating that the stream dependency is exclusive.
     * @param streamDependency A 31-bit stream identifier for the stream that this stream depends on.
     * @param weight           An unsigned 8-bit integer representing a priority weight for the stream.
     *                         Add one to the value to obtain a weight between 1 and 256.
     */
    public PriorityFrame(int streamId, boolean E, int streamDependency, short weight) {
        super(streamId, 5, PRIORITY);
        if (streamDependency < 0) {
            throw new IllegalArgumentException("Invalid stream dependency");
        }
        this.E = E;
        this.streamDependency = streamDependency;
        this.weight = (short) ((weight & 0xff) + 1);
    }

    /**
     * Crates a priority frame with the specified flags, streamId and payload.
     *
     * @param flags    the flags of this frame.
     * @param streamId the stream id of this frame.
     * @param payload  the payload of this frame.
     */
    public PriorityFrame(byte flags, int streamId, ByteBuffer payload) {
        super(streamId, payload.remaining(), PRIORITY, flags);
        int next = payload.getInt();
        this.E = (next & -2147483648) != 0;
        this.streamDependency = next & 2147483647;
        this.weight = (short) ((payload.get() & 0xff) + 1);
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        out.putInt(E ? streamDependency & -2147483648 : streamDependency); // -2147483648 is only the first bit
        out.put((byte) (weight - 1));
        return out.flip();
    }

    @Override
    public String toString() {
        return super.toString() + ", E=" + E + ", streamDependency=" + streamDependency + ", weight=" + weight;
    }
}
