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

        Socket s = ss.accept();

        OutputStream os = s.getOutputStream();
        os.write(new byte[]{(byte) 0x5052, (byte) 0x4920, (byte) 0x2a20, (byte) 0x4854, (byte) 0x5450, (byte) 0x2f32, (byte) 0x2e30, (byte) 0x0d0a, (byte) 0x0d0a, (byte) 0x534d, (byte) 0x0d0a, (byte) 0x0d0a});

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
