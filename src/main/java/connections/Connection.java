package connections;

import frames.*;
import streams.Stream;
import streams.StreamState;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static frames.ErrorCode.FRAME_SIZE_ERROR;
import static streams.StreamState.*;

public class Connection {

    private Settings settings = Settings.getDefault();
    private int idIncrement = 1;
    private Map<Integer, Stream> streamMap = new HashMap<>();
    private Socket socket;
    private Stream root;

    /**
     * Creates a connection with a socket.
     *
     * @param connection the socket to send data over.
     */
    public Connection(Socket connection) {
        this.socket = connection;
        this.root = new Stream(0, null);
        addStreamInternal(root);

        Thread t = new ConnectionThread(this);
        t.start();
    }

    void onRecieveData(ByteBuffer frame) {
        int next = frame.getInt();
        int length = next >> 8; // length is only 3 bytes
        byte type = (byte) (next & 0xff);
        byte flags = frame.get();
        Stream stream = streamMap.get(frame.getInt() & Integer.MAX_VALUE); // mask away the R bit
        if (frame.remaining() != length) {
            throw FRAME_SIZE_ERROR.error();
        }
        // remaining bytes in data is payload
        Frame f;
        FrameType ft = FrameType.from(type);
        // TODO act upon the recieved data
        switch (ft) {
            case DATA:
                f = new DataFrame(flags, frame.slice());

            case HEADERS:
                f = new HeadersFrame(flags, frame.slice());

            case PRIORITY:
                f = new PriorityFrame(flags, frame.slice());

            case RST_STREAM:
                f = new RSTStreamFrame(flags, frame.slice());

            case SETTINGS:
                f = new SettingsFrame(flags, frame.slice());

            case PUSH_PROMISE:
                f = new PushPromiseFrame(flags, frame.slice());

            case PING:
                f = new PingFrame(flags, frame.slice());

            case GOAWAY:
                f = new GoAwayFrame(flags, frame.slice());

            case WINDOW_UPDATE:
                f = new WindowUpdateFrame(flags, frame.slice());

            case CONTINUATION:
                f = new ContinuationFrame(flags, frame.slice());

        }
    }

    private void addStreamInternal(Stream s) {
        streamMap.put(s.streamId, s);
    }

    private boolean isAllowed(Stream s, Frame f) {
        StreamState ss = s.getState();
        switch (f.getType()) {
            case DATA:
                return s.streamId != 0 && (ss == OPEN || ss == HALF_CLOSED_LOCAL);
            case HEADERS:
                return s.streamId != 0 && (ss == IDLE || ss == RESERVED_LOCAL || ss == OPEN || ss == HALF_CLOSED_REMOTE);
            case PRIORITY:
            case CONTINUATION:
                return s.streamId != 0;
            case RST_STREAM:
                return ss != IDLE && s.streamId != 0;
            case SETTINGS:
            case PING:
            case GOAWAY:
                return s.streamId == 0;
            case PUSH_PROMISE:
                return ss == OPEN || ss == HALF_CLOSED_LOCAL; // TODO check settings
            case WINDOW_UPDATE:
                return true;
            default:
                return false;
        }
    }

    public boolean sendWithRoot(Frame f) throws IOException {
        return sendFrame(root, f);
    }

    public boolean sendFrame(Stream s, Frame f) throws IOException {
        // TODO connection should figure out which frame to send with on its own
        if (isAllowed(s, f)) {
            ByteBuffer frame = f.bytes(s.streamId);
            while (frame.hasRemaining()) {
                socket.getOutputStream().write(frame.get());
            }
            return true;
        }
        return false;
    }

    public Stream addStream() {
        Stream s = new Stream(idIncrement++, root);
        addStreamInternal(s);
        // TODO send headers frame
        return s;
    }

    void setDependency(Stream s, int dependency, boolean exclusive) {
        Stream t = streamMap.get(dependency);
        t.setParent(s.parent);
        s.setParent(t);
        if (exclusive) {
            for (Stream stream : streamMap.values()) {
                if (stream.parent == t && stream != s) {
                    s.parent = s;
                }
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
