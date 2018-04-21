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
    private Socket connection;
    private Stream root;

    public Connection(Socket connection) {
        this.connection = connection;
        this.root = new Stream(0, null);
        addStreamInternal(root);
    }

    void onRecieveData(ByteBuffer frame) {
        int length = (((frame.get() << 8) & frame.get()) << 8) & frame.get();
        byte type = frame.get();
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

    public boolean sendFrame(Stream s, Frame f) throws IOException {
        if (isAllowed(s, f)) {
            ByteBuffer payload = f.bytes(s.streamId);
            while (payload.hasRemaining()) {
                connection.getOutputStream().write(payload.get());
            }
            return true;
        }
        return false;
    }

    public Stream addStream() {
        Stream s = new Stream(idIncrement++, root);
        addStreamInternal(s);
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
}
