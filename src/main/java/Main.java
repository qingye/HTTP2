import connections.Connection;
import connections.Settings;
import frames.DataFrame;
import frames.SettingsFrame;
import streams.Stream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {

//        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
//
//        ServerSocket ss = ssf.createServerSocket(1234);

        ServerSocket ss = new ServerSocket(1234);
        System.out.println(new String(new byte[]{(byte) 0x50, 0x52, (byte) 0x49, 0x20, (byte) 0x2a, 0x20, (byte) 0x48, 0x54, (byte) 0x54, 0x50, (byte) 0x2f, 0x32, (byte) 0x2e, 0x30, (byte) 0x0d, 0x0a, (byte) 0x0d, 0x0a, (byte) 0x53, 0x4d, (byte) 0x0d, 0x0a, (byte) 0x0d, 0x0a}));

        Socket s = ss.accept();

        OutputStream os = s.getOutputStream();
        os.write(new byte[]{(byte) 0x50, 0x52, (byte) 0x49, 0x20, (byte) 0x2a, 0x20, (byte) 0x48, 0x54, (byte) 0x54, 0x50, (byte) 0x2f, 0x32, (byte) 0x2e, 0x30, (byte) 0x0d, 0x0a, (byte) 0x0d, 0x0a, (byte) 0x53, 0x4d, (byte) 0x0d, 0x0a, (byte) 0x0d, 0x0a});

        ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/hello.html")));

        Connection c = new Connection(s);

        c.sendWithRoot(new SettingsFrame(false, Settings.getDefault()));

        Stream stream = c.addStream();
        ByteBuffer bb = new DataFrame(data, (byte) 0, true).bytes(stream.streamId);
        byte[] bytes = new byte[bb.remaining()];
        bb.get(bytes);
        os.write(bytes);
    }
}
