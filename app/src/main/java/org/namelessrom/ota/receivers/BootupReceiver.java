package org.namelessrom.ota.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.namelessrom.ota.Device;
import org.namelessrom.ota.UpdateEntry;
import org.namelessrom.ota.utils.IOUtils;
import org.namelessrom.ota.utils.Utils;

import java.io.File;

public class BootupReceiver extends BroadcastReceiver {
    private static final String TAG = "BootupReceiver";

    @Override public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "--> checking if update got applied");
        new AsyncTask<Void, Void, Void>() {
            @Override protected Void doInBackground(Void... voids) {
                final File file = new File(IOUtils.DOWNLOAD_PATH, "update.zip.info");
                final String content = Utils.readFromFile(file);
                if (TextUtils.isEmpty(content)) {
                    Log.d(TAG, "--> update information does not exist or is empty, exit");
                    return null;
                }

                UpdateEntry entry;
                try {
                    entry = new Gson().fromJson(content, UpdateEntry.class);
                } catch (JsonSyntaxException ignored) {
                    entry = null;
                }
                if (entry == null) {
                    Log.d(TAG, "--> update information is not valid, exit");
                    return null;
                }

                if (Device.get().date >= entry.timestamp) {
                    Log.d(TAG, "--> installed build is newer or equal to update, delete update");
                    new File(IOUtils.DOWNLOAD_PATH, "update.zip").delete();
                    file.delete();
                }

                Log.d(TAG, "--> done");
                return null;
            }
        }.execute();
    }
}
