package org.namelessrom.ota;

import android.app.ActionBar;
import android.app.Activity;
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

public class SystemUpdateActivity extends Activity implements UpdateListener {
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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) { // check for updates, if we gain focus
            checkForUpdates();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_update);

        // setup our action bar
        setupActionBar();

        mTitle = (TextView) findViewById(R.id.title);
        mMessage = (TextView) findViewById(R.id.message);
        mAction = (Button) findViewById(R.id.action_button);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mUpdateAvailable) {
                    installUpdate();
                } else {
                    checkForUpdates();
                }
            }
        });

        getLastChecked();
    }

    private void getLastChecked() {
        // check if we have updates available
        String tmp = PreferenceHelper.get(this).getString(PreferenceHelper.PREF_UPDATE_AVAIL, null);
        mUpdateAvailable = !TextUtils.isEmpty(tmp);
        if (mUpdateAvailable) {
            try {
                mUpdateEntry = new UpdateEntry(new JSONObject(tmp));
            } catch (JSONException jse) {
                Logger.e(this, String.format("Could not create JSONObject from: %s", tmp), jse);
                mUpdateEntry = null;
            }
        }
        mUpdateAvailable = mUpdateEntry != null;
        if (mTitle != null) {
            mTitle.setText(mUpdateAvailable ? R.string.update_avail : R.string.update_not_avail);
        }
        if (mAction != null) {
            mAction.setText(mUpdateAvailable ? R.string.update_system : R.string.check_now);
        }

        if (mMessage != null) {
            mMessage.setText(getString(R.string.last_checked, getTime()));
        }
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
        if (mAction != null) mAction.setEnabled(false);
        if (mUpdater == null) {
            mUpdater = new Updater(this, this);
        }
        mUpdater.check();
    }

    private void installUpdate() {
        // TODO: install update, smartass
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateCheckFinished(final boolean success, final UpdateEntry entry) {
        if (!success) {
            if (mMessage != null) {
                final String text = getString(R.string.last_checked, getTime())
                        + '\n'
                        + getString(R.string.update_check_failed);
                mMessage.setText(text);
            }
            return;
        }
        PreferenceHelper.get(SystemUpdateActivity.this)
                .putString(PreferenceHelper.PREF_LAST_CHECKED,
                        String.valueOf(System.currentTimeMillis()))
                .putString(PreferenceHelper.PREF_UPDATE_AVAIL, entry.json);
        if (mAction != null) mAction.setEnabled(true);
        getLastChecked();
    }
}
