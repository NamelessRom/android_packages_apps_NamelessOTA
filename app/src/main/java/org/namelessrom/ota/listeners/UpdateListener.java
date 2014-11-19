package org.namelessrom.ota.listeners;

import android.annotation.Nullable;

import org.namelessrom.ota.UpdateEntry;

public interface UpdateListener {
    public void updateCheckFinished(final boolean success, @Nullable final UpdateEntry updateEntry);
}
