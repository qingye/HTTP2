import connections.Connection;
import frames.DataFrame;
import streams.Stream;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {

//        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
//
//        ServerSocket ss = ssf.createServerSocket(80);
//
//        SSLSocket s = (SSLSocket) ss.accept();
//        s.startHandshake();


        ServerSocket ss = new ServerSocket(80);

        Socket s = ss.accept();

        System.out.println("Connected...");


        Connection c = new Connection(s);

        ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/hello.html")));
        Stream stream = c.addStream();
        DataFrame df = new DataFrame(data, (byte) 0, false);
        c.sendFrame(stream, df);
    }
}
