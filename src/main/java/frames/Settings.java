package frames;

import static frames.ErrorCode.FLOW_CONTROL_ERROR;
import static frames.ErrorCode.PROTOCOL_ERROR;

/**
 * Settings for settings frames
 *
 * @author Rolv-Arild Braaten
 * @see frames.SettingsFrame
 */
public enum Settings {
    /**
     * SETTINGS_HEADER_TABLE_SIZE (0x1):  Allows the sender to inform the
     * remote endpoint of the maximum size of the header compression
     * table used to decode header blocks, in octets.  The encoder can
     * select any size equal to or less than this value by using
     * signaling specific to the header compression format inside a
     * header block (see [COMPRESSION]).  The initial value is 4,096
     * octets.
     */
    SETTINGS_HEADER_TABLE_SIZE(0x1, 4096),

    /**
     * SETTINGS_ENABLE_PUSH (0x2):  This setting can be used to disable
     * server push (Section 8.2).  An endpoint MUST NOT send a
     * PUSH_PROMISE frame if it receives this parameter set to a value of
     * 0.  An endpoint that has both set this parameter to 0 and had it
     * acknowledged MUST treat the receipt of a PUSH_PROMISE frame as a
     * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
     * <p>
     * The initial value is 1, which indicates that server push is
     * permitted.  Any value other than 0 or 1 MUST be treated as a
     * connection error (Section 5.4.1) of type PROTOCOL_ERROR.
     */
    SETTINGS_ENABLE_PUSH(0x2, 1),

    /**
     * SETTINGS_MAX_CONCURRENT_STREAMS (0x3):  Indicates the maximum number
     * of concurrent streams that the sender will allow.  This limit is
     * directional: it applies to the number of streams that the sender
     * permits the receiver to create.  Initially, there is no limit to
     * this value.  It is recommended that this value be no smaller than
     * 100, so as to not unnecessarily limit parallelism.
     * <p>
     * A value of 0 for SETTINGS_MAX_CONCURRENT_STREAMS SHOULD NOT be
     * treated as special by endpoints.  A zero value does prevent the
     * creation of new streams; however, this can also happen for any
     * limit that is exhausted with active streams.  Servers SHOULD only
     * set a zero value for short durations; if a server does not wish to
     * accept requests, closing the connection is more appropriate.
     */
    SETTINGS_MAX_CONCURRENT_STREAMS(0x3, 100),

    /**
     * SETTINGS_INITIAL_WINDOW_SIZE (0x4):  Indicates the sender's initial
     * window size (in octets) for stream-level flow control.  The
     * initial value is 2^16-1 (65,535) octets.
     * <p>
     * This setting affects the window size of all streams (see
     * Section 6.9.2).
     * <p>
     * Values above the maximum flow-control window size of 2^31-1 MUST
     * be treated as a connection error (Section 5.4.1) of type
     * FLOW_CONTROL_ERROR.
     */
    SETTINGS_INITIAL_WINDOW_SIZE(0x4, 65535),

    /**
     * SETTINGS_MAX_FRAME_SIZE (0x5):  Indicates the size of the largest
     * frame payload that the sender is willing to receive, in octets.
     * <p>
     * The initial value is 2^14 (16,384) octets.  The value advertised
     * by an endpoint MUST be between this initial value and the maximum
     * allowed frame size (2^24-1 or 16,777,215 octets), inclusive.
     * Values outside this range MUST be treated as a connection error
     * (Section 5.4.1) of type PROTOCOL_ERROR.
     */
    SETTINGS_MAX_FRAME_SIZE(0x5, 16384),
    /**
     * SETTINGS_MAX_HEADER_LIST_SIZE (0x6):  This advisory setting informs a
     * peer of the maximum size of header list that the sender is
     * prepared to accept, in octets.  The value is based on the
     * uncompressed size of header fields, including the length of the
     * name and value in octets plus an overhead of 32 octets for each
     * header field.
     * <p>
     * For any given request, a lower limit than what is advertised MAY
     * be enforced.  The initial value of this setting is unlimited.
     */
    SETTINGS_MAX_HEADER_LIST_SIZE(0x6, Integer.MAX_VALUE);

    final short code;
    private int val;

    Settings(int code, int initialValue) {
        this.code = (short) code;
        this.val = initialValue;
    }

    public void setVal(int val) {
        switch (this) {
            case SETTINGS_ENABLE_PUSH:
                if (val != 0 && val != 1) throw PROTOCOL_ERROR.error();
            case SETTINGS_INITIAL_WINDOW_SIZE:
                if (val < 0) throw FLOW_CONTROL_ERROR.error(); // the value is above 2^31-1 in two's complement
            case SETTINGS_MAX_FRAME_SIZE:
                if (val < 16384 || val > 16777215) throw PROTOCOL_ERROR.error();
            default:
                this.val = val;
        }
    }
}
