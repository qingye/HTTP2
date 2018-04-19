package frames;

public enum ErrorCode {
    NO_ERROR(0x0, ""), PROTOCOL_ERROR(0x1, "Protocol error"), INTERNAL_ERROR(0x2, "Internal error"),
    FLOW_CONTROL_ERROR(0x3, "Flow control error"), SETTINGS_TIMEOUT(0x4, "Settings timeout"),
    STREAM_CLOSED(0x5, "Stream closed"), FRAME_SIZE_ERROR(0x6, "Frame size error"),
    REFUSED_STREAM(0x7, "Refused stream"), CANCEL(0x8, "Cancel"), COMPRESSION_ERROR(0x9, "Compression error"),
    CONNECT_ERROR(0xa, "Connect error"), ENHANCE_YOUR_CALM(0xb, "Excessive load"),
    INADEQUATE_SECURITY(0xc, "Inadequate security"), HTTP_1_1_REQUIRED(0xd, "HTTP/1.1 required");

    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Error error() {
        throw new Error(message);
    }
}
