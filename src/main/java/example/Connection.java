package example;

import connections.AbstractConnection;
import connections.ConnectionSettings;
import frames.*;
import streams.Stream;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Connection extends AbstractConnection {
    /**
     * Creates a connection with a socket.
     *
     * @param socket the socket to send data over.
     */
    public Connection(Socket socket) {
        super(socket);
    }

    @Override
    public void onDataFrame(DataFrame df) {
        System.out.println("Recv: " + df);
    }

    @Override
    public void onHeadersFrame(HeadersFrame hf) {
        System.out.println("Recv: " + hf);
        Stream stream = streamMap.get(hf.streamId);
        if (stream == null) {
            stream = new Stream(hf.streamId, streamMap.get(hf.streamDependency));
            addStream(stream);
        }

        try {
//            addStream(new Stream(stream.streamId, root));
            String s = ":status:200\r\ncontent-length:155\r\ncontent-type:text/html;charset=utf-8\r\n";
            HeadersFrame hah = new HeadersFrame(stream.streamId, false, true, (byte) 0, ByteBuffer.wrap(s.getBytes("UTF-8")), true, 0, (short) 256);
            sendFrame(hah);
            ByteBuffer bf = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/hello.html")));
            DataFrame html = new DataFrame(stream.streamId, bf, true);
            sendFrame(html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPriorityFrame(PriorityFrame pf) {
        System.out.println("Recv: " + pf);
    }

    @Override
    public void onRSTStreamFrame(RSTStreamFrame rsf) {
        System.out.println("Recv: " + rsf);
    }

    @Override
    public void onSettingsFrame(SettingsFrame sf) {
        System.out.println("Recv: " + sf);
        if (!Flags.isSet(sf.flags, Flags.ACK)) {
            this.settings.setSettings(sf.settings);
            try {
                sendFrame(root, new SettingsFrame(0, true, ConnectionSettings.getUndefined()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPushPromiseFrame(PushPromiseFrame ppf) {
        System.out.println("Recv: " + ppf);
    }

    @Override
    public void onPingFrame(PingFrame pf) throws IOException {
        System.out.println("Recv: " + pf);
        sendFrame(root, new PingFrame(0, true, pf.opaqueData));
    }

    @Override
    public void onGoAwayFrame(GoAwayFrame gaf) {
        System.out.println("Recv: " + gaf);
    }

    @Override
    public void onWindowUpdateFrame(WindowUpdateFrame wuf) {
        System.out.println("Recv: " + wuf);
    }

    @Override
    public void onContinuationFrame(ContinuationFrame cf) {
        System.out.println("Recv: " + cf);
    }
}
