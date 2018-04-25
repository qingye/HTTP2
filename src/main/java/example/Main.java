package example;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Main {

    private static final int HTTP_PORT = 443;
    private static final String KEYSTORE_LOCATION = "src/main/resources/heltNy.jks";
    private static final String KEYSTORE_PASSWORD = "123456";

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);

//        System.setProperty("javax.net.debug", "ssl:record");

        try {
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(HTTP_PORT);
            SSLParameters sp = serverSocket.getSSLParameters();
            sp.setApplicationProtocols(new String[]{"h2"});
            serverSocket.setSSLParameters(sp);

            while (!Thread.currentThread().isInterrupted()) {
                SSLSocket client = (SSLSocket) serverSocket.accept();
                if (client.isConnected()) {
                    Connection c = new Connection(client);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
