package org.namelessrom.ota;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.namelessrom.ota.listeners.UpdateListener;
import org.namelessrom.ota.utils.Logger;

public class Updater implements Response.Listener<JSONArray>, Response.ErrorListener {
    public static final String SF_URL =
            "https://sourceforge.net/projects/namelessrom/files/n-2.0/%s/";

    private static final String URL = "https://nameless-rom.org/update/%s/single";

    private RequestQueue mQueue;
    private UpdateListener mListener;

    public Updater(final Context context, final UpdateListener listener) {
        mListener = listener;

        mQueue = Volley.newRequestQueue(context);
    }

    public void check() {
        final JsonArrayRequest jsObjRequest = new JsonArrayRequest(getUrl(), this, this);
        mQueue.add(jsObjRequest);
    }

    private String getUrl() {
        return String.format(URL, Device.get().name);
    }

    @Override
    public void onResponse(final JSONArray jsonArray) {
        Logger.v(this, "onResponse: %s", jsonArray.toString());
        UpdateEntry updateEntry = null;
        if (jsonArray.length() > 0) {
            try {
                updateEntry = new UpdateEntry(jsonArray.getJSONObject(0));
            } catch (JSONException jse) {
                Logger.e(this, "can not create UpdateEntry from jsonArray", jse);
            }
        }
        // notify the update listener
        if (mListener != null) mListener.updateCheckFinished(true, updateEntry);
    }


    @Override
    public void onErrorResponse(final VolleyError volleyError) {
        Logger.v(this, "onErrorResponse: %s", volleyError.toString());
        if (Logger.getEnabled()) volleyError.fillInStackTrace();
        // notify the update listener
        if (mListener != null) mListener.updateCheckFinished(false, null);
    }
}
