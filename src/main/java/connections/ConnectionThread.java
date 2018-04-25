package connections;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

class ConnectionThread extends Thread {

    private final AbstractConnection connection;

    ConnectionThread(AbstractConnection connection) {
        this.connection = connection;
    }


    // Taken from https://stackoverflow.com/questions/9666783/java-inputstream-wait-for-data

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024); // off heap memory.
            try {
                readLength(bb, 9);
                int length = bb.getInt(0) >>> 8;
                if (length > bb.capacity())
                    bb = ByteBuffer.allocateDirect(length);
                readLength(bb, length);
                bb.flip();

                // process buffer.
                connection.onReceiveData(bb);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readLength(ByteBuffer bb, int length) throws IOException {
//        bb.clear();
//        bb.mark();
        bb.limit(length+9);
        InputStream is = connection.getSocket().getInputStream();
//        System.out.print(new String(is.readAllBytes()));
        int re;
        while (bb.remaining() > 0 && (re = is.read()) >= 0) {
//            System.out.println(bb.remaining());
            bb.put((byte) re);
        }
//        byte[] bytes = new byte[bb.position()];
//        bb.reset();
//        bb.get(bytes);
//        System.out.println(new String(bytes));
//        if (bb.remaining() > 0) throw new EOFException();
    }
}
