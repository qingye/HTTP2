import frames.Setting;

import static frames.Setting.*;

/**
 * A class to represent connection settings
 */
public class Settings {

    private int[] values;

    public Settings(int headerTableSize, int enablePush, int maxConcurrentStreams, int initialWindowSize, int maxFrameSize, int maxHeaderListSize) {
        values[0] = headerTableSize;
        values[1] = enablePush;
        values[2] = maxConcurrentStreams;
        values[3] = initialWindowSize;
        values[4] = maxFrameSize;
        values[5] = maxHeaderListSize;
    }

    public static Settings getDefault() {
        return new Settings(
                SETTINGS_HEADER_TABLE_SIZE.defaultValue,
                SETTINGS_ENABLE_PUSH.defaultValue,
                SETTINGS_MAX_CONCURRENT_STREAMS.defaultValue,
                SETTINGS_INITIAL_WINDOW_SIZE.defaultValue,
                SETTINGS_MAX_FRAME_SIZE.defaultValue,
                SETTINGS_MAX_HEADER_LIST_SIZE.defaultValue
        );
    }

    public void setValue(Setting setting, int value) {
        values[setting.code - 1] = value;
    }
}
