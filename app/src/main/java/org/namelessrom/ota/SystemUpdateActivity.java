package org.namelessrom.ota;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.namelessrom.ota.listeners.UpdateListener;
import org.namelessrom.ota.utils.DownloadHelper;
import org.namelessrom.ota.utils.Logger;
import org.namelessrom.ota.utils.PreferenceHelper;
import org.namelessrom.ota.utils.Utils;

public class SystemUpdateActivity extends Activity implements UpdateListener, DownloadHelper.DownloadCallback {
    private static final String TAG = "SystemUpdateActivity";

    private TextView mTitle;
    private TextView mMessage;
    private Button mAction;

    private UpdateEntry mUpdateEntry;
    private boolean mUpdateAvailable = false;

    private Updater mUpdater;

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: remove
        Logger.setEnabled(true);
        Logger.v(TAG, Device.get().toString());

        DownloadHelper.registerCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadHelper.unregisterCallback();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_update);

        DownloadHelper.init(this, this);

        // setup our action bar
        setupActionBar();

        mTitle = (TextView) findViewById(R.id.title);

        mMessage = (TextView) findViewById(R.id.message);
        mMessage.setText(getString(R.string.last_checked, getTime()));

        mAction = (Button) findViewById(R.id.action_button);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mUpdateAvailable) {
                    installUpdate();
                } else if (DownloadHelper.isDownloading()) {
                    DownloadHelper.cancelDownload();
                } else {
                    checkForUpdates();
                }
            }
        });

        getLastChecked();
        checkForUpdates();
    }

    private void getLastChecked() {
        if (DownloadHelper.isDownloading()) {

            return;
        }
        // check if we have updates available
        String tmp = PreferenceHelper.get(this).getString(PreferenceHelper.PREF_UPDATE_AVAIL, null);
        mUpdateAvailable = !TextUtils.isEmpty(tmp);
        // check for null or if it is empty, if not, convert it to an update entry
        if (mUpdateAvailable) {
            try {
                mUpdateEntry = new UpdateEntry(new JSONObject(tmp));
            } catch (JSONException jse) {
                Logger.e(this, String.format("Could not create JSONObject from: %s", tmp), jse);
                mUpdateEntry = null;
            }
        }
        // if our update entry is not null, compare the timestamps
        mUpdateAvailable = mUpdateEntry != null && (Utils.getBuildDate() < mUpdateEntry.timestamp);

        mTitle.setText(mUpdateAvailable ? R.string.update_avail : R.string.update_not_avail);
        mAction.setText(mUpdateAvailable ? R.string.update_system : R.string.check_now);
    }

    private String getTime() {
        long timestamp = Utils.tryParseLong(
                PreferenceHelper.get(this).getString(PreferenceHelper.PREF_LAST_CHECKED, "0"));

        if (timestamp <= 0) { // let's treat an parsing error as never checked
            timestamp = System.currentTimeMillis();
        }

        final int flags;
        if (DateUtils.isToday(timestamp)) {
            // if we last checked today, only show the time
            flags = DateUtils.FORMAT_SHOW_TIME;
        } else {
            // else show the time and date
            flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE;
        }

        // convert the timestamp into a real date, using the flags from above
        return DateUtils.formatDateTime(this, timestamp, flags);
    }

    private void checkForUpdates() {
        mAction.setEnabled(false);
        if (mUpdater == null) {
            mUpdater = new Updater(this, this);
        }
        mUpdater.check();
    }

    private void installUpdate() {
        if (mUpdateEntry == null) {
            // TODO: throw error
            return;
        }
        DownloadHelper
                .downloadFile(mUpdateEntry.downloadurl, mUpdateEntry.filename, mUpdateEntry.md5sum);
    }

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            // action bar is null, panic and get out of here!
            return;
        }

        // display home as up to finish the activity and return to settings
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // inflate our action bar items, nothing special
        getMenuInflater().inflate(R.menu.menu_system_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                // finish our activity and return to settings
                finish();
                return true;
            }
            case R.id.action_all_builds: {
                final String url = String.format(Updater.SF_URL, Device.get().getName());
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ane) {
                    Logger.e(this, "could not start activity", ane);
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateCheckFinished(final boolean success, final UpdateEntry entry) {
        mAction.setEnabled(true);
        String text = getString(R.string.last_checked, getTime()) + '\n' + '\n';
        if (!success) {
            text += getString(R.string.update_check_failed);
        } else {
            text += String.format("%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s",
                    getString(R.string.device), entry.codename,
                    getString(R.string.channel), entry.channel,
                    getString(R.string.date), entry.timestamp,
                    getString(R.string.filename), entry.filename,
                    getString(R.string.md5), entry.md5sum);

            PreferenceHelper.get(SystemUpdateActivity.this)
                    .putString(PreferenceHelper.PREF_LAST_CHECKED,
                            String.valueOf(System.currentTimeMillis()))
                    .putString(PreferenceHelper.PREF_UPDATE_AVAIL, entry.json);
        }

        if (mMessage != null) {
            mMessage.setText(text);
        }

        getLastChecked();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null
                && intent.getBooleanExtra(Utils.CHECK_DOWNLOADS_FINISHED, false)) {
            DownloadHelper.checkDownloadFinished(this,
                    intent.getLongExtra(Utils.CHECK_DOWNLOADS_ID, -1L));
        }
    }

    @Override public void onDownloadStarted() {

    }

    @Override public void onDownloadProgress(int progress) {

    }

    @Override public void onDownloadFinished(Uri uri, String md5) {

    }

    @Override public void onDownloadError(String reason) {

    }
}
