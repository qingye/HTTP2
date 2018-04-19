package frames;

/**
 * All errors and their corresponding codes
 */
public enum ErrorCode {
    /**
     * NO_ERROR (0x0):  The associated condition is not a result of an
     * error.  For example, a GOAWAY might include this code to indicate
     * graceful shutdown of a connection.
     */
    NO_ERROR(0x0, ""),

    /**
     * PROTOCOL_ERROR (0x1):  The endpoint detected an unspecific protocol
     * error.  This error is for use when a more specific error code is
     * not available.
     */
    PROTOCOL_ERROR(0x1, "Protocol error"),

    /**
     * INTERNAL_ERROR (0x2):  The endpoint encountered an unexpected
     * internal error.
     */
    INTERNAL_ERROR(0x2, "Internal error"),

    /**
     * FLOW_CONTROL_ERROR (0x3):  The endpoint detected that its peer
     * violated the flow-control protocol.
     */
    FLOW_CONTROL_ERROR(0x3, "Flow control error"),

    /**
     * SETTINGS_TIMEOUT (0x4):  The endpoint sent a SETTINGS frame but did
     * not receive a response in a timely manner.  See Section 6.5.3
     * ("Settings Synchronization").
     */
    SETTINGS_TIMEOUT(0x4, "Settings timeout"),

    /**
     * STREAM_CLOSED (0x5):  The endpoint received a frame after a stream
     * was half-closed.
     */
    STREAM_CLOSED(0x5, "Stream closed"),

    /**
     * FRAME_SIZE_ERROR (0x6):  The endpoint received a frame with an
     * invalid size.
     */
    FRAME_SIZE_ERROR(0x6, "Frame size error"),

    /**
     * REFUSED_STREAM (0x7):  The endpoint refused the stream prior to
     * performing any application processing (see Section 8.1.4 for
     * details).
     */
    REFUSED_STREAM(0x7, "Refused stream"),

    /**
     * CANCEL (0x8):  Used by the endpoint to indicate that the stream is no
     * longer needed.
     */
    CANCEL(0x8, "Cancel"),

    /**
     * COMPRESSION_ERROR (0x9):  The endpoint is unable to maintain the
     * header compression context for the connection.
     */
    COMPRESSION_ERROR(0x9, "Compression error"),

    /**
     * CONNECT_ERROR (0xa):  The connection established in response to a
     * CONNECT request (Section 8.3) was reset or abnormally closed.
     */
    CONNECT_ERROR(0xa, "Connect error"),

    /**
     * ENHANCE_YOUR_CALM (0xb):  The endpoint detected that its peer is
     * exhibiting a behavior that might be generating excessive load.
     */
    ENHANCE_YOUR_CALM(0xb, "Excessive load"),

    /**
     * INADEQUATE_SECURITY (0xc):  The underlying transport has properties
     * that do not meet minimum security requirements (see Section 9.2).
     */
    INADEQUATE_SECURITY(0xc, "Inadequate security"),

    /**
     * HTTP_1_1_REQUIRED (0xd):  The endpoint requires that HTTP/1.1 be used
     * instead of HTTP/2.
     */
    HTTP_1_1_REQUIRED(0xd, "HTTP/1.1 required");

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
