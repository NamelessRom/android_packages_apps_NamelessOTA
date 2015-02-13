package org.namelessrom.ota;

import android.os.SystemProperties;

/**
 * Represents the device
 */
public class Device {
    public static final String UNKNOWN = "unknown";

    private static final Device sInstance = new Device();

    public final String name; // ro.product.device
    public final int date;    // ro.nameless.date

    private Device() {
        name = SystemProperties.get("ro.product.device", UNKNOWN);
        date = SystemProperties.getInt("ro.nameless.date", -1);
    }

    public static Device get() {
        return sInstance;
    }

    public String toString() {
        return String.format("Name: %s", name);
    }

}
