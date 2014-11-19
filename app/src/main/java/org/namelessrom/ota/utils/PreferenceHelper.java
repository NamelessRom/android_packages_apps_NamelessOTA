package org.namelessrom.ota.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
    private static PreferenceHelper sInstance;

    private static SharedPreferences sPrefs;

    public static final String PREF_LAST_CHECKED = "pref_last_checked";
    public static final String PREF_UPDATE_AVAIL = "pref_update_avail";

    private PreferenceHelper(final Context context) {
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceHelper get(final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceHelper(context);
        }
        return sInstance;
    }

    public String getString(final String key, final String def) {
        return sPrefs.getString(key, def);
    }

    public PreferenceHelper putString(final String key, final String value) {
        sPrefs.edit().putString(key, value).apply();
        return sInstance;
    }
}
