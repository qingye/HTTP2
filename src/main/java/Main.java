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
        os.write(("HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: h2c\r\n" +
                "\r\n").getBytes());

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
