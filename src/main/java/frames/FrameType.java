package frames;

/**
 * All the frame types with their corresponding codes and classes.
 *
 * The classes are used to instantiate frames from raw data.
 * This requires that all classes extending Frame MUST provide
 * a constructor with parameters {@code (byte flags, ByteBuffer payload)}
 * to work correctly when receiving.
 *
 * @author Rolv-Arild Braaten
 * @see frames.Frame
 */
public enum FrameType {
    /**
     * @see DataFrame
     */
    DATA(0x0, DataFrame.class),

    /**
     * @see HeadersFrame
     */
    HEADERS(0x1, HeadersFrame.class),

    /**
     * @see PriorityFrame
     */
    PRIORITY(0x2, PriorityFrame.class),

    /**
     * @see RSTStreamFrame
     */
    RST_STREAM(0x3, RSTStreamFrame.class),

    /**
     * @see SettingsFrame
     */
    SETTINGS(0x4, SettingsFrame.class),

    /**
     * @see PushPromiseFrame
     */
    PUSH_PROMISE(0x5, PushPromiseFrame.class),

    /**
     * @see PingFrame
     */
    PING(0x6, PingFrame.class),

    /**
     * @see GoAwayFrame
     */
    GOAWAY(0x7, GoAwayFrame.class),

    /**
     * @see WindowUpdateFrame
     */
    WINDOW_UPDATE(0x8, WindowUpdateFrame.class),

    /**
     * @see ContinuationFrame
     */
    CONTINUATION(0x9, ContinuationFrame.class);

    byte code;
    Class<?> c;

    FrameType(int code, Class<?> c) {
        this.code = (byte) code;
        this.c = c;
    }

    public static FrameType from(byte code) {
        return values()[code];
    }
}
