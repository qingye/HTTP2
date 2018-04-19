package frames;

import java.nio.ByteBuffer;

import static frames.FrameType.*;

public class PushPromiseFrame extends Frame {

    byte padLength;
    int promisedStreamID;

    PushPromiseFrame(byte padLength, int promisedStreamID, int flags, int streamId, ByteBuffer headerBlockFragment) {
        super(PUSH_PROMISE, flags, streamId, headerBlockFragment); // TODO format payload
    }
}
