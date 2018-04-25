package connections;

import frames.Setting;

import static frames.Setting.*;

/**
 * A class to represent settings for a connection.
 */
public class ConnectionSettings {

    public static int UNDEFINED = Integer.MIN_VALUE;

    private int[] values;

    /**
     * Creates settings with the specified values.
     *
     * @param headerTableSize      Allows the sender to inform the
     *                             remote endpoint of the maximum size of the header compression
     *                             table used to decode header blocks, in octets.
     * @param enablePush           This setting can be used to disable
     *                             server push.  An endpoint MUST NOT send a
     *                             PUSH_PROMISE frame if it receives this parameter set to a value of
     *                             0.
     * @param maxConcurrentStreams Indicates the maximum number
     *                             of concurrent streams that the sender will allow.
     * @param initialWindowSize    Indicates the sender's initial
     *                             window size (in octets) for stream-level flow control.
     * @param maxFrameSize         Indicates the size of the largest
     *                             frame payload that the sender is willing to receive, in octets.
     * @param maxHeaderListSize    This advisory setting informs a
     *                             peer of the maximum size of header list that the sender is
     *                             prepared to accept, in octets.
     */
    public ConnectionSettings(int headerTableSize, int enablePush, int maxConcurrentStreams, int initialWindowSize, int maxFrameSize, int maxHeaderListSize) {
        values = new int[6];
        values[0] = headerTableSize;
        values[1] = enablePush;
        values[2] = maxConcurrentStreams;
        values[3] = initialWindowSize;
        values[4] = maxFrameSize;
        values[5] = maxHeaderListSize;
    }

    /**
     * Changes all the settings to new settings, unless the new setting is undefined.
     *
     * @param newSettings the new settings to use.
     */
    public void setSettings(ConnectionSettings newSettings) {
        for (int i = 0; i < values.length; i++) {
            int val = newSettings.values[i];
            if (val != UNDEFINED) {
                values[i] = val;
            }
        }
    }

    /**
     * @return settings containing only undefined values.
     */
    public static ConnectionSettings getUndefined() {
        return new ConnectionSettings(UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
    }

    /**
     * @return settings containing all the default values.
     */
    public static ConnectionSettings getDefault() {
        return new ConnectionSettings(
                SETTINGS_HEADER_TABLE_SIZE.defaultValue,
                SETTINGS_ENABLE_PUSH.defaultValue,
                SETTINGS_MAX_CONCURRENT_STREAMS.defaultValue,
                SETTINGS_INITIAL_WINDOW_SIZE.defaultValue,
                SETTINGS_MAX_FRAME_SIZE.defaultValue,
                SETTINGS_MAX_HEADER_LIST_SIZE.defaultValue
        );
    }

    /**
     * Sets the value of a setting.
     *
     * @param setting the setting to set the value of.
     * @param newVal the new value of this setting.
     */
    public void setValue(Setting setting, int newVal) {
        values[setting.code - 1] = newVal;
    }

    /**
     * Finds the value of a specific setting.
     *
     * @param setting the setting to find the value of.
     * @return the value of the specified setting.
     */
    public int valueOf(Setting setting) {
        return values[setting.code - 1];
    }

    /**
     * @return the values of all the settings.
     */
    public int[] values() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Settings: [");
        for (int i = 0; i < values.length; i++) {
            Setting setting = Setting.values()[i];
            if (values[i] == UNDEFINED) {
                s.append(setting.toString()).append("=UNDEFINED");
            } else if (values[i] == setting.defaultValue) {
                s.append(setting.toString()).append("=DEFAULT").append("(").append(values[i]).append(")");
            } else {
                s.append(setting.toString()).append("=").append(values[i]);
            }
            if (i < values.length - 1) s.append(", ");
        }
        s.append("]");
        return s.toString();
    }
}
