import connections.Connection;
import frames.DataFrame;
import streams.Stream;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException, KeyStoreException, UnrecoverableKeyException {

        SSLContext context = SSLContext.getInstance("TLSv1.2");

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        char[] password = "123456".toCharArray();
        KeyStore ks = KeyStore.getInstance(new File("C:\\Program Files\\Java\\jdk-10.0.1\\bin\\keystore"), password);
        kmf.init(ks, password);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ks);

        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecureRandom.getInstance("SHA1PRNG"));
        SSLServerSocketFactory ssf = context.getServerSocketFactory();

//        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();

        ServerSocket ss = ssf.createServerSocket(80);

        SSLSocket s = (SSLSocket) ss.accept();
        s.startHandshake();


//        ServerSocket ss = new ServerSocket(80);
//
//        Socket s = ss.accept();

        System.out.println("Connected...");


        Connection c = new Connection(s);

        ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(Paths.get("src/main/resources/hello.html")));
        Stream stream = c.addStream();
        DataFrame df = new DataFrame(data, (byte) 0, false);
        c.sendFrame(stream, df);
    }
}
