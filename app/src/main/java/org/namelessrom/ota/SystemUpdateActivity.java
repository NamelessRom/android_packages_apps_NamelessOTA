package org.namelessrom.ota;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SystemUpdateActivity extends Activity {
    private static final String TAG = "SystemUpdateActivity";

    private TextView mTitle;
    private TextView mMessage;
    private Button mAction;

    private boolean mUpdateAvailable = false;

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, Device.get().toString());
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
        mUpdateAvailable = Utils.tryParseInt(
                PreferenceHelper.get(this).getString(PreferenceHelper.PREF_UPDATE_AVAIL, "0")) == 1;
        mTitle.setText(mUpdateAvailable ? R.string.update_avail : R.string.update_not_avail);
        mAction.setText(mUpdateAvailable ? R.string.update_system : R.string.check_now);

        long timestamp = Utils.tryParseLong(
                PreferenceHelper.get(this).getString(PreferenceHelper.PREF_LAST_CHECKED, "0"));
        boolean shouldCheck = false;

        if (timestamp <= 0) { // let's treat an parsing error as never checked
            // schedule a check and set last checked to now
            shouldCheck = true;
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
        final String time = DateUtils.formatDateTime(this, timestamp, flags);

        mMessage.setText(getString(R.string.last_checked, time));

        if (shouldCheck) {
            checkForUpdates();
        }
    }

    private void checkForUpdates() {
        // TODO: check for updates ;P
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
        //actionBar.setDisplayShowHomeEnabled(true);
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
}
