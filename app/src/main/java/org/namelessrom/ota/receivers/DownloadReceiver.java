package org.namelessrom.ota.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.namelessrom.ota.SystemUpdateActivity;
import org.namelessrom.ota.utils.Utils;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        Intent i = new Intent(context, SystemUpdateActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Utils.CHECK_DOWNLOADS_FINISHED, true);
        i.putExtra(Utils.CHECK_DOWNLOADS_ID, id);
        context.startActivity(i);
    }

}
