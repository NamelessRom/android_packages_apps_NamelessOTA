package org.namelessrom.ota;

/**
 * Who can live nowadays without Utils class?
 */
public class Utils {
    private static final String TAG = "Utils";

    public static int tryParseInt(final String intToParse) {
        try {
            return Integer.parseInt(intToParse);
        } catch (NumberFormatException nfe) {
            Logger.e(TAG, String.format("Could not parse: %s", intToParse), nfe);
            return -1;
        }
    }

    public static long tryParseLong(final String longToParse) {
        try {
            return Long.parseLong(longToParse);
        } catch (NumberFormatException nfe) {
            Logger.e(TAG, String.format("Could not parse: %s", longToParse), nfe);
            return -1;
        }
    }

}
