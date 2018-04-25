package streams;

import frames.Frame;

import static streams.StreamState.OPEN;

/**
 * A "stream" is an independent, bidirectional sequence of frames
 * exchanged between the client and server within an HTTP/2 connection.
 * Streams have several important characteristics:
 * <ul>
 * <li>A single HTTP/2 connection can contain multiple concurrently open
 * streams, with either endpoint interleaving frames from multiple
 * streams.</li>
 * <li>Streams can be established and used unilaterally or shared by
 * either the client or server.</li>
 * <li>Streams can be closed by either endpoint.</li>
 * <li>The order in which frames are sent on a stream is significant.
 * Recipients process frames in the order they are received.  In
 * particular, the order of HEADERS and DATA frames is semantically
 * significant.</li>
 * <li>Streams are identified by an integer. Stream identifiers are
 * assigned to streams by the endpoint initiating the stream.
 * </ul>
 *
 * @author Rolv-Arild Braaten
 * @see StreamState
 */
public class Stream {

    public int streamId;
    StreamState state;
    public Stream parent;
    byte weight;

    /**
     * Creates a stream with the specified stream id and parent.
     *
     * @param streamId The stream id of this stream.
     * @param parent The parent of this stream.
     */
    public Stream(int streamId, Stream parent) {
        this(streamId, parent, (byte) 1);
    }

    /**
     * Creates a stream with the specified stream id, parent and weight.
     *
     * @param streamId The stream id of this stream.
     * @param parent The parent of this stream.
     * @param weight The relative weight of this stream.
     */
    public Stream(int streamId, Stream parent, byte weight) {
        this.streamId = streamId;
        this.state = OPEN;
        this.parent = parent;
        this.weight = weight;
    }

    /**
     * @return the state of this stream.
     */
    public StreamState getState() {
        return state;
    }

    /**
     * @param newState the new state of this stream
     */
    public void setState(StreamState newState) {
        this.state = newState;
    }


    /**
     * @param newParent the new parent of this stream.
     */
    public void setParent(Stream newParent) {
        this.parent = newParent;
    }

    /**
     * @param newWeight the new relative weight of this stream.
     */
    public void setWeight(byte newWeight) {
        this.weight = newWeight;
    }
}
