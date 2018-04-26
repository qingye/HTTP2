package connections;

import frames.*;
import streams.Stream;
import streams.StreamState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static frames.ErrorCode.FRAME_SIZE_ERROR;
import static frames.ErrorCode.HTTP_1_1_REQUIRED;
import static streams.StreamState.*;

/**
 * An abstract class to use when creating connections.
 */
public abstract class AbstractConnection implements ConnectionInterface {

    protected ConnectionSettings settings = ConnectionSettings.getDefault();
    protected int idIncrement = 1;
    protected Map<Integer, Stream> streamMap = new HashMap<>();
    protected Socket socket;
    protected Stream root;
    protected Thread thread;

    /**
     * Creates a connection with a socket.
     *
     * @param socket the socket to send data over.
     */
    public AbstractConnection(Socket socket) {
        this.socket = socket;
        this.root = new Stream(0, null);
        addStream(root);

        try {
            onFirstRequest();
            this.thread = new ConnectionThread(this);
            this.thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFirstRequest() throws IOException {
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String s = is.readLine();
        if (s == null) return;
        if (s.equals("PRI * HTTP/2.0")) {
            System.out.println("Client request for HTTP/2.0");
            ConnectionSettings cs = ConnectionSettings.getUndefined();
            sendFrame(new SettingsFrame(0, false, cs));
        } else {
            throw HTTP_1_1_REQUIRED.error();
        }
    }

    @Override
    public void onReceiveData(ByteBuffer frame) throws IOException {
        int next = frame.getInt();
        int length = next >>> 8; // length is only 3 first bytes
        byte type = (byte) (next & 0xff);
        byte flags = frame.get();
        int streamId = frame.getInt() & Integer.MAX_VALUE;
        this.idIncrement = streamId + 1;
        if (frame.remaining() != length) {
            throw FRAME_SIZE_ERROR.error();
        }
        // remaining bytes in data is payload
        FrameType ft = FrameType.from(type);
        // TODO act upon the recieved data
        switch (ft) {
            case DATA:
                DataFrame df = new DataFrame(flags, streamId, frame.slice());
                onDataFrame(df);
                break;
            case HEADERS:
                HeadersFrame hf = new HeadersFrame(flags, streamId, frame.slice());
                onHeadersFrame(hf);
                break;
            case PRIORITY:
                PriorityFrame prf = new PriorityFrame(flags, streamId, frame.slice());
                onPriorityFrame(prf);
                break;
            case RST_STREAM:
                RSTStreamFrame rsf = new RSTStreamFrame(flags, streamId, frame.slice());
                onRSTStreamFrame(rsf);
                break;
            case SETTINGS:
                SettingsFrame sf = new SettingsFrame(flags, streamId, frame.slice());
                onSettingsFrame(sf);
                break;
            case PUSH_PROMISE:
                PushPromiseFrame ppf = new PushPromiseFrame(flags, streamId, frame.slice());
                onPushPromiseFrame(ppf);
                break;
            case PING:
                PingFrame pif = new PingFrame(flags, streamId, frame.slice());
                onPingFrame(pif);
                break;
            case GOAWAY:
                GoAwayFrame gaf = new GoAwayFrame(flags, streamId, frame.slice());
                onGoAwayFrame(gaf);
                break;
            case WINDOW_UPDATE:
                WindowUpdateFrame wuf = new WindowUpdateFrame(flags, streamId, frame.slice());
                onWindowUpdateFrame(wuf);
                break;
            case CONTINUATION:
                ContinuationFrame cf = new ContinuationFrame(flags, streamId, frame.slice());
                onContinuationFrame(cf);
                break;
        }
    }

    /**
     * Adds the specified stream to the stream map.
     *
     * @param s The stream to add to the stream map.
     */
    public Stream addStream(Stream s) {
        streamMap.put(s.streamId, s);
        return s;
    }


    private boolean isAllowed(Stream s, Frame f) {
        StreamState ss = s.getState();
        switch (f.type) {
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
                return settings.valueOf(Setting.SETTINGS_ENABLE_PUSH) != 0 && s.streamId != 0 && (ss == OPEN || ss == HALF_CLOSED_LOCAL);
            case WINDOW_UPDATE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Sends a frame with the stream id specified in the frame.
     *
     * @param f The frame to send.
     * @return False if this frame is not allowed to be sent on the specified stream.
     * @throws IOException If there is an error sending the frame.
     */
    public boolean sendFrame(Frame f) throws IOException {
        return sendFrame(streamMap.get(f.streamId), f);
    }

    /**
     * Sends a frame with the stream id specified in the frame.
     *
     * @param f The frame to send.
     * @param s The stream to send this frame with.
     * @return False if this frame is not allowed to be sent on the specified stream.
     * @throws IOException If there is an error sending the frame.
     */
    public boolean sendFrame(Stream s, Frame f) throws IOException {
        // TODO connection should figure out which frame to send with on its own (flow control)
        if (isAllowed(s, f)) {
            f.streamId = s.streamId;
            ByteBuffer frame = f.bytes();
            byte[] b = new byte[frame.remaining()];
            for (int i = 0; i < b.length; i++) {
                b[i] = frame.get();
            }


            if (f.type == FrameType.HEADERS) {
                HeadersFrame hf = (HeadersFrame) f;
                byte[] bytes = new byte[b.length - 9 - 4 - 1];

                System.arraycopy(b, 9 + 4 + 1, bytes, 0, bytes.length);

//                System.out.println(Arrays.toString(b));
//                System.out.println(Arrays.toString(bytes));

//                System.out.println(decompress(bytes));
            }

//            System.out.println(Arrays.toString(b));


            socket.getOutputStream().write(b);
            socket.getOutputStream().flush();
//            while (frame.hasRemaining()) {
//                socket.getOutputStream().write(frame.get());
//            }
            System.out.println("Send: " + f);
            return true;
        }
        return false;
    }

    /**
     * Adds a stream and returns it.
     *
     * @return the new stream.
     */
    public Stream addStream() {
        Stream s = new Stream(idIncrement++, root);
        addStream(s);
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
