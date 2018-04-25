package connections;

import frames.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An interface representing a HTTP/2 connection.
 */
public interface ConnectionInterface {

    /**
     * Specifies what to do on first request.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onFirstRequest() throws IOException;

    /**
     * Specifies what to do when data is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onReceiveData(ByteBuffer data) throws IOException;

    /**
     * Specifies what to do when a data frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onDataFrame(DataFrame df) throws IOException;

    /**
     * Specifies what to do when a headers frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onHeadersFrame(HeadersFrame hf) throws IOException;

    /**
     * Specifies what to do when a priority frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onPriorityFrame(PriorityFrame pf) throws IOException;

    /**
     * Specifies what to do when a RST stream frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onRSTStreamFrame(RSTStreamFrame rsf) throws IOException;

    /**
     * Specifies what to do when a settings frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onSettingsFrame(SettingsFrame sf) throws IOException;

    /**
     * Specifies what to do when a push promise frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onPushPromiseFrame(PushPromiseFrame ppf) throws IOException;

    /**
     * Specifies what to do when a ping frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onPingFrame(PingFrame pf) throws IOException;

    /**
     * Specifies what to do when a goaway frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onGoAwayFrame(GoAwayFrame gaf) throws IOException;

    /**
     * Specifies what to do when a window update frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onWindowUpdateFrame(WindowUpdateFrame wuf) throws IOException;

    /**
     * Specifies what to do when a continuation frame is received.
     *
     * @throws IOException if there is an error connecting to the endpoint.
     */
    void onContinuationFrame(ContinuationFrame cf) throws IOException;

}
