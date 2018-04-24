package connections;

import frames.*;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ConnectionInterface {

    void onFirstRequest() throws IOException;

    void onReceiveData(ByteBuffer data) throws IOException;

    void onDataFrame(DataFrame df) throws IOException;

    void onHeadersFrame(HeadersFrame hf) throws IOException;

    void onPriorityFrame(PriorityFrame pf) throws IOException;

    void onRSTStreamFrame(RSTStreamFrame rsf) throws IOException;

    void onSettingsFrame(SettingsFrame sf) throws IOException;

    void onPushPromiseFrame(PushPromiseFrame ppf) throws IOException;

    void onPingFrame(PingFrame pf) throws IOException;

    void onGoAwayFrame(GoAwayFrame gaf) throws IOException;

    void onWindowUpdateFrame(WindowUpdateFrame wuf) throws IOException;

    void onContinuationFrame(ContinuationFrame cf) throws IOException;

}
