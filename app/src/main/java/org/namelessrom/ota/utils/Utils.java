package org.namelessrom.ota.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Who can live nowadays without Utils class?
 */
public class Utils {
    private static final String TAG = "Utils";

    public static final String CHECK_DOWNLOADS_FINISHED = "org.namelessrom.ota.CHECK_DOWNLOADS_FINISHED";
    public static final String CHECK_DOWNLOADS_ID = "org.namelessrom.ota.CHECK_DOWNLOADS_ID";

    public static int tryParseInt(final String intToParse) {
        return tryParseInt(intToParse, -1);
    }

    public static int tryParseInt(final String intToParse, final int def) {
        try {
            return Integer.parseInt(intToParse);
        } catch (NumberFormatException nfe) {
            Logger.e(TAG, String.format("Could not parse: %s", intToParse), nfe);
            return def;
        }
    }

    public static long tryParseLong(final String longToParse) {
        return tryParseLong(longToParse, -1);
    }

    public static long tryParseLong(final String longToParse, final long def) {
        try {
            return Long.parseLong(longToParse);
        } catch (NumberFormatException nfe) {
            Logger.e(TAG, String.format("Could not parse: %s", longToParse), nfe);
            return def;
        }
    }

    public static String readBuildProp(final String prop) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader("/system/build.prop");
            bufferedReader = new BufferedReader(fileReader, 512);
            String tmp;
            while ((tmp = bufferedReader.readLine()) != null) {
                if (tmp.contains(prop)) return tmp.replace(prop + "=", "");
            }
        } catch (final Exception e) {
            Logger.e(TAG, "an error occurred", e);
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
            } catch (Exception ignored) { }
            try {
                if (fileReader != null) fileReader.close();
            } catch (Exception ignored) { }
        }

        return "NULL";
    }

    public static int getBuildDate() {
        return tryParseInt(readBuildProp("ro.nameless.date"), 20140101);
    }

    public static String getCommandResult(String command) {
        Process p = null;
        DataOutputStream os = null;
        try {
            p = Runtime.getRuntime().exec(command);
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            return getStreamLines(p.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ignored) { }
            if (p != null) p.destroy();
        }
    }

    private static String getStreamLines(final InputStream is) {
        String out = null;
        StringBuffer buffer = null;
        DataInputStream dis = null;

        try {
            dis = new DataInputStream(is);
            if (dis.available() > 0) {
                buffer = new StringBuffer(dis.readLine());
                while (dis.available() > 0) {
                    buffer.append("\n").append(dis.readLine());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (dis != null) { dis.close(); }
            } catch (IOException ignored) { }
        }
        if (buffer != null) {
            out = buffer.toString();
        }
        return out;
    }
}
