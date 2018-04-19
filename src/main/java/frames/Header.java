package frames;

import java.nio.ByteBuffer;

import static frames.ErrorCode.*;
import static frames.FrameType.*;

/**
 * A class to represent a frame header.
 */
class Header {
    private static final int MAX_LENGTH = 16777216; // 2^24
    private static int SETTINGS_MAX_FRAME_SIZE = 16384;

    int length;
    final FrameType type;
    byte flags;
    int streamId;

    /**
     * Constructs a frame header
     *
     * @param length   The length of the frame payload expressed as an unsigned 24-bit integer.
     *                 Values greater than 214 (16,384) MUST NOT be sent unless the receiver has set a larger value for SETTINGS_MAX_FRAME_SIZE.
     * @param type     The 8-bit type of the frame.
     *                 The frame type determines the format and semantics of the frame. Implementations MUST ignore and discard any frame that has a type that is unknown.
     * @param flags    An 8-bit field reserved for boolean flags specific to the frame type.
     *                 Flags are assigned semantics specific to the indicated frame type. Flags that have no defined semantics for a particular frame type MUST be ignored and MUST be left unset (0x0) when sending.
     * @param streamId A stream Id expressed as an unsigned 31-bit integer.
     *                 The value 0x0 is reserved for frames that are associated with the connection as a whole as opposed to an individual stream.
     */
    Header(int length, FrameType type, byte flags, int streamId) {
        if (length < 0 || length > SETTINGS_MAX_FRAME_SIZE || length > MAX_LENGTH) {
            throw FRAME_SIZE_ERROR.error();
        } else if (type == PING && length != 8) {
            throw FRAME_SIZE_ERROR.error();
        }
        if (streamId < 0) throw new IllegalArgumentException("Invalid stream");
        this.length = length;
        this.type = type;
        this.flags = flags;
        this.streamId = streamId;
    }
}
