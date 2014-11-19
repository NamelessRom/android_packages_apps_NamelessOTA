package org.namelessrom.ota;

import android.os.SystemProperties;

/**
 * Represents the device
 */
public class Device {
    public static final String UNKNOWN = "unknown";

    private static Device sInstance;
    private static String name; // ro.product.device

    private Device() {
        name = SystemProperties.get("ro.product.device", UNKNOWN);
    }

    public static Device get() {
        if (sInstance == null) {
            sInstance = new Device();
        }
        return sInstance;
    }

    public String toString() {
        return String.format("Name: %s", name);
    }

    public String getName() { return name; }
}
