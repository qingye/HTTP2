import connections.Connection;
import connections.Settings;
import frames.SettingsFrame;
import streams.Stream;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1234);

        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();

        Socket s = ss.accept();

        Connection c = new Connection(s);

        Stream stream = c.addStream();

        c.sendWithRoot(new SettingsFrame(false, Settings.getDefault()));
    }
}
