import frames.HeadersFrame;
import streams.Stream;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(4444);

        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();

        Socket s = ss.accept();

        Connection c = new Connection(s);

        Stream stream = c.addStream();

        c.sendFrame(stream, new HeadersFrame(true, true, (byte) 0, ByteBuffer.allocate(0)));
    }
}
