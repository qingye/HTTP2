package frames;

/**
 * All the frame types with their corresponding codes
 */
enum FrameType {
    DATA(0x0), HEADERS(0x1), PRIORITY(0x2), RST_STREAM(0x3), SETTINGS(0x4), PUSH_PROMISE(0x5),
    PING(0x6), GOAWAY(0x7), WINDOW_UPDATE(0x8), CONTINUATION(0x9);

    byte code;

    FrameType(int code) {
        this.code = (byte) code;
    }
}
