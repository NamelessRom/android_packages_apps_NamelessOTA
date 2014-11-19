package org.namelessrom.ota.listeners;

import org.namelessrom.ota.UpdateEntry;

public interface UpdateListener {
    public void updateCheckFinished(final boolean success, final UpdateEntry updateEntry);
}
