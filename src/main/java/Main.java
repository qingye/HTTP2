import connections.Connection;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.net.Socket;

public class Main {

    private static final int HTTP_PORT = 443;
    private static final String KEYSTORE_LOCATION = "C:/Keys/heltNy.jks";
    private static final String KEYSTORE_PASSWORD = "123456";

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

        System.setProperty("javax.net.debug", "ssl:record");

        try {
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            SSLServerSocket serversocket = (SSLServerSocket) ssf.createServerSocket(HTTP_PORT);

            while (!Thread.currentThread().isInterrupted()) {
                Socket client = serversocket.accept();
                Connection c = new Connection(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
