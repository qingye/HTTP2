package connections;

import frames.Setting;

import static frames.Setting.*;

/**
 * A class to represent connection settings
 */
public class Settings {

    public static int UNDEFINED = Integer.MIN_VALUE;

    private int[] values;

    public Settings(int headerTableSize, int enablePush, int maxConcurrentStreams, int initialWindowSize, int maxFrameSize, int maxHeaderListSize) {
        values = new int[6];
        values[0] = headerTableSize;
        values[1] = enablePush;
        values[2] = maxConcurrentStreams;
        values[3] = initialWindowSize;
        values[4] = maxFrameSize;
        values[5] = maxHeaderListSize;
    }

    public void setSettings(Settings newSettings) {
        for (int i = 0; i < values.length; i++) {
            int val = newSettings.values[i];
            if (val != UNDEFINED) {
                values[i] = val;
            }
        }
    }

    public static Settings getUndefined() {
        return new Settings(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
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

    public int valueOf(Setting setting) {
        return values[setting.code - 1];
    }

    public int[] values() {
        return values;
    }
}
