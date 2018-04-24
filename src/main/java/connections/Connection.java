package connections;

import frames.*;
import streams.Stream;
import streams.StreamState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static frames.ErrorCode.FRAME_SIZE_ERROR;
import static frames.ErrorCode.HTTP_1_1_REQUIRED;
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
     * @param socket the socket to send data over.
     */
    public Connection(Socket socket) {
        this.socket = socket;
        this.root = new Stream(0, null);
        addStreamInternal(root);

        try {
            onFirstRequest();
            Thread t = new ConnectionThread(this);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void onFirstRequest() throws IOException {
        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String s = is.readLine();
//            while ((s = is.readLine()) == null); // await first message

        if (s.equals("PRI * HTTP/2.0")) {
            System.out.println("Client request for HTTP/2.0");
            sendFrame(new SettingsFrame(0, false, Settings.getUndefined()));
        } else {
            throw HTTP_1_1_REQUIRED.error();
        }
    }

    void onRecieveData(ByteBuffer frame) {
        int next = frame.getInt();
        int length = next >>> 8; // length is only 3 first bytes
        byte type = (byte) (next & 0xff);
        byte flags = frame.get();
        int streamId = frame.getInt() & Integer.MAX_VALUE;
        Stream stream = streamMap.get(streamId); // mask away the R bit
        if (frame.remaining() != length) {
            throw FRAME_SIZE_ERROR.error();
        }
        // remaining bytes in data is payload
        FrameType ft = FrameType.from(type);
        // TODO act upon the recieved data
        switch (ft) {
            case DATA:
                DataFrame df = new DataFrame(flags, streamId, frame.slice());
                System.out.println(df.streamId);

            case HEADERS:
                HeadersFrame hf = new HeadersFrame(flags, streamId, frame.slice());
                if (stream == null) {
                    stream = new Stream(streamId, streamMap.get(hf.streamDependency));
                    addStreamInternal(stream);
                }

                try {
                    addStreamInternal(new Stream(++streamId, root));
                    HeadersFrame hah = new HeadersFrame(streamId, true, true, (byte) 0, ByteBuffer.wrap("Content-Type: text/html\r\n\r\n".getBytes()));
                    sendFrame(hah);
                    ByteBuffer bf = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/hello.html")));
                    DataFrame html = new DataFrame(streamId, bf, false);
                    sendFrame(html);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case PRIORITY:
                PriorityFrame prf = new PriorityFrame(flags, stream.streamId, frame.slice());

            case RST_STREAM:
                RSTStreamFrame rstsf = new RSTStreamFrame(flags, stream.streamId, frame.slice());

            case SETTINGS:
                SettingsFrame sf = new SettingsFrame(flags, stream.streamId, frame.slice());
                this.settings.setSettings(sf.settings);
                break;
            case PUSH_PROMISE:
                PushPromiseFrame ppf = new PushPromiseFrame(flags, stream.streamId, frame.slice());

            case PING:
                PingFrame pif = new PingFrame(flags, stream.streamId, frame.slice());

            case GOAWAY:
                GoAwayFrame gaf = new GoAwayFrame(flags, stream.streamId, frame.slice());

            case WINDOW_UPDATE:
                WindowUpdateFrame wuf = new WindowUpdateFrame(flags, stream.streamId, frame.slice());

            case CONTINUATION:
                ContinuationFrame cf = new ContinuationFrame(flags, stream.streamId, frame.slice());

        }
    }

    private void addStreamInternal(Stream s) {
        streamMap.put(s.streamId, s);
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
                return ss == OPEN || ss == HALF_CLOSED_LOCAL; // TODO check settings
            case WINDOW_UPDATE:
                return true;
            default:
                return false;
        }
    }

    public boolean sendFrame(Frame f) throws IOException {
        return sendFrame(streamMap.get(f.streamId), f);
    }

    public boolean sendFrame(Stream s, Frame f) throws IOException {
        // TODO connection should figure out which frame to send with on its own
        if (isAllowed(s, f)) {
            ByteBuffer frame = f.bytes(s.streamId);
            byte[] b = frame.array();
            socket.getOutputStream().write(b);
            socket.getOutputStream().flush();
//            while (frame.hasRemaining()) {
//                socket.getOutputStream().write(frame.get());
//            }
            return true;
        }
        return false;
    }

    public Stream addStream() throws IOException {
        Stream s = new Stream(idIncrement++, root);
        addStreamInternal(s);
        sendFrame(s, new HeadersFrame(s.streamId, (byte) 0, false, s.parent.streamId, (byte) 1, ByteBuffer.allocate(0), true, false));
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
