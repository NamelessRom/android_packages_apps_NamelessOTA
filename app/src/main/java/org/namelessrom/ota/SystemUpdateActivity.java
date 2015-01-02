package org.namelessrom.ota;

import android.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.namelessrom.ota.listeners.UpdateListener;
import org.namelessrom.ota.utils.DownloadHelper;
import org.namelessrom.ota.utils.IOUtils;
import org.namelessrom.ota.utils.Logger;
import org.namelessrom.ota.utils.PreferenceHelper;
import org.namelessrom.ota.utils.RebootHelper;
import org.namelessrom.ota.utils.RecoveryHelper;
import org.namelessrom.ota.utils.Utils;

import java.io.File;

public class SystemUpdateActivity extends Activity implements UpdateListener, DownloadHelper.DownloadCallback {
    private static final String TAG = "SystemUpdateActivity";

    private TextView mTitle;
    private ProgressBar mDownloadProgress;
    private TextView mMessage;
    private Button mAction;
    private Button mExtraAction;

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
        mDownloadProgress = (ProgressBar) findViewById(R.id.download_progress);

        mMessage = (TextView) findViewById(R.id.message);
        mMessage.setText(getString(R.string.last_checked, getTime()));

        mAction = (Button) findViewById(R.id.action_button);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (DownloadHelper.isDownloading()) {
                    DownloadHelper.cancelDownload();
                } else if (DownloadHelper.isDownloaded()) {
                    installUpdate();
                } else if (mUpdateAvailable) {
                    downloadUpdate();
                } else {
                    checkForUpdates();
                }
            }

        });

        mExtraAction = (Button) findViewById(R.id.extra_action_button);
        mExtraAction.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Logger.v(SystemUpdateActivity.this, "Deleting update",
                        new File(IOUtils.SDCARD, "update.zip").delete());
                updateUi();
            }
        });

        updateUi();
        checkForUpdates();
    }

    private void updateUi() {
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

        if (DownloadHelper.isDownloading()) {
            mTitle.setText(R.string.downloading_system_update);
            mMessage.setText(getString(R.string.download_get_notified) + '\n' + '\n'
                    + getUpdateEntryMsg(mUpdateEntry));
            mAction.setText(R.string.cancel_download);
            return;
        } else if (DownloadHelper.isDownloaded()) {
            mTitle.setText(R.string.system_update_ready);
            mMessage.setText(R.string.system_update_ready_message);
            mAction.setText(R.string.install_update);
            mExtraAction.setVisibility(View.VISIBLE);
            return;
        }

        mTitle.setText(mUpdateAvailable ? R.string.update_avail : R.string.update_not_avail);
        mMessage.setText(getString(R.string.last_checked, getTime()) + '\n' + '\n'
                + getUpdateEntryMsg(mUpdateEntry));
        mAction.setText(mUpdateAvailable ? R.string.download_update : R.string.check_now);
        mExtraAction.setVisibility(View.INVISIBLE);
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

    private void downloadUpdate() {
        if (mUpdateEntry == null || DownloadHelper.isDownloading()) {
            // TODO: throw error?
            return;
        }
        DownloadHelper.downloadFile(mUpdateEntry.downloadurl, "update.zip", mUpdateEntry.md5sum);
    }

    private void installUpdate() {
        new RebootHelper(new RecoveryHelper(this)).showRebootDialog(this);
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
    public void updateCheckFinished(final boolean success, @Nullable final UpdateEntry entry) {
        mAction.setEnabled(true);
        if (!success || entry == null) {
            Toast.makeText(this, R.string.update_check_failed, Toast.LENGTH_SHORT).show();
        } else {
            PreferenceHelper.get(SystemUpdateActivity.this)
                    .putString(PreferenceHelper.PREF_LAST_CHECKED,
                            String.valueOf(System.currentTimeMillis()))
                    .putString(PreferenceHelper.PREF_UPDATE_AVAIL, entry.json);
        }

        updateUi();
    }

    private String getUpdateEntryMsg(final UpdateEntry entry) {
        if (entry == null) return "";
        return String.format("%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s",
                getString(R.string.device), entry.codename,
                getString(R.string.channel), entry.channel,
                getString(R.string.date), entry.timestamp,
                getString(R.string.filename), entry.filename,
                getString(R.string.md5), entry.md5sum);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null && intent.getExtras() != null
                && intent.getBooleanExtra(Utils.CHECK_DOWNLOADS_FINISHED, false)) {
            DownloadHelper.checkDownloadFinished(this,
                    intent.getLongExtra(Utils.CHECK_DOWNLOADS_ID, -1L));
        }
    }

    @Override
    public void onDownloadStarted() {
        if (mDownloadProgress != null) mDownloadProgress.setProgress(0);
        updateUi();
    }

    @Override
    public void onDownloadProgress(int progress) {
        if (mDownloadProgress != null) mDownloadProgress.setProgress(progress);
    }

    @Override
    public void onDownloadFinished(Uri uri, String md5) {
        if (mDownloadProgress != null) mDownloadProgress.setProgress(0);
        updateUi();
    }

    @Override
    public void onDownloadError(String reason) {
        if (mDownloadProgress != null) mDownloadProgress.setProgress(0);
        updateUi();
    }
}
