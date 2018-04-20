package frames;

/**
 * All the frame types with their corresponding codes
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public enum FrameType {
    /**
     * @see DataFrame
     */
    DATA(0x0),

    /**
     * @see HeadersFrame
     */
    HEADERS(0x1),

    /**
     * @see PriorityFrame
     */
    PRIORITY(0x2),

    /**
     * @see RSTStreamFrame
     */
    RST_STREAM(0x3),

    /**
     * @see SettingsFrame
     */
    SETTINGS(0x4),

    /**
     * @see PushPromiseFrame
     */
    PUSH_PROMISE(0x5),

    /**
     * @see PingFrame
     */
    PING(0x6),

    /**
     * @see GoAwayFrame
     */
    GOAWAY(0x7),

    /**
     * @see WindowUpdateFrame
     */
    WINDOW_UPDATE(0x8),

    /**
     * @see ContinuationFrame
     */
    CONTINUATION(0x9);

    byte code;

    FrameType(int code) {
        this.code = (byte) code;
    }
}
