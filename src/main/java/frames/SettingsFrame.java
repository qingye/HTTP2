package frames;

import connections.Settings;

import java.nio.ByteBuffer;

import static connections.Settings.UNDEFINED;
import static frames.Flags.ACK;
import static frames.FrameType.SETTINGS;

/**
 * The SETTINGS frame (type=0x4) conveys configuration parameters that
 * affect how endpoints communicate, such as preferences and constraints
 * on peer behavior.  The SETTINGS frame is also used to acknowledge the
 * receipt of those parameters.  Individually, a SETTINGS parameter can
 * also be referred to as a "setting".
 * <p>
 * SETTINGS parameters are not negotiated; they describe characteristics
 * of the sending peer, which are used by the receiving peer.  Different
 * values for the same parameter can be advertised by each peer.  For
 * example, a client might set a high initial flow-control window,
 * whereas a server might set a lower value to conserve resources.
 * <p>
 * A SETTINGS frame MUST be sent by both endpoints at the start of a
 * connection and MAY be sent at any other time by either endpoint over
 * the lifetime of the connection.  Implementations MUST support all of
 * the parameters defined by this specification.
 * <p>
 * Each parameter in a SETTINGS frame replaces any existing value for
 * that parameter.  Parameters are processed in the order in which they
 * appear, and a receiver of a SETTINGS frame does not need to maintain
 * any state other than the current value of its parameters.  Therefore,
 * the value of a SETTINGS parameter is the last value that is seen by a
 * receiver.
 * <p>
 * SETTINGS parameters are acknowledged by the receiving peer.  To
 * enable this, the SETTINGS frame defines the following flag:
 * <p>
 * ACK (0x1):  When set, bit 0 indicates that this frame acknowledges
 * receipt and application of the peer's SETTINGS frame.  When this
 * bit is set, the payload of the SETTINGS frame MUST be empty.
 * Receipt of a SETTINGS frame with the ACK flag set and a length
 * field value other than 0 MUST be treated as a connection error
 * (Section 5.4.1) of type FRAME_SIZE_ERROR.  For more information,
 * see Section 6.5.3 ("Settings Synchronization").
 * <p>
 * SETTINGS frames always apply to a connection, never a single stream.
 * The stream identifier for a SETTINGS frame MUST be zero (0x0).  If an
 * endpoint receives a SETTINGS frame whose stream identifier field is
 * anything other than 0x0, the endpoint MUST respond with a connection
 * error (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * The SETTINGS frame affects connection state.  A badly formed or
 * incomplete SETTINGS frame MUST be treated as a connection error
 * (Section 5.4.1) of type PROTOCOL_ERROR.
 * <p>
 * A SETTINGS frame with a length other than a multiple of 6 octets MUST
 * be treated as a connection error (Section 5.4.1) of type
 * FRAME_SIZE_ERROR.
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public class SettingsFrame extends Frame {

    private final Settings settings;

    /**
     * Constructs a settings frame
     *
     * @param ack When set, bit 0 indicates that this frame acknowledges receipt and application of the peer's SETTINGS frame.
     *            When this bit is set, the payload of the SETTINGS frame MUST be empty.
     */
    public SettingsFrame(boolean ack, Settings settings) {
        super(findLength(settings), SETTINGS, (ack ? ACK : 0)); // TODO format payload, parameter for settings
        this.settings = settings;
    }

    private static int findLength(Settings settings) {
        int c = 0;
        for (int i : settings.values()) {
            if (i != UNDEFINED) {
                c++;
            }
        }
        return c * 6;
    }

    public SettingsFrame(byte flags, ByteBuffer payload) {
        super(payload.remaining(), SETTINGS, flags);
        Settings sets = Settings.getUndefined();
        while (payload.hasRemaining()) {
            Setting set = Setting.from(payload.getShort());
            int val = payload.getInt();
            sets.setValue(set, val);
        }
        this.settings = sets;
    }

    @Override
    public ByteBuffer payload() {
        ByteBuffer out = ByteBuffer.allocate(length);
        for (Setting setting : Setting.values()) {
            int val = settings.valueOf(setting);
            if (val != UNDEFINED) {
                out.putShort(setting.code);
                out.putInt(val);
            }
        }
        return (ByteBuffer) out.rewind();
    }
}
