package org.namelessrom.ota;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an UpdateEntry
 * <p/>
 * Example json:
 * [{
 * "channel":"NIGHTLY",
 * "filename":"nameless-4.4.4-20140731-find7u-NIGHTLY.zip",
 * "md5sum":"1a3019cb8a488454cfe3ae06bdb992a9",
 * "downloadurl":"http://sourceforge.net/projects/namelessrom/files/find7u/nameless-4.4.4-20140731-find7u-NIGHTLY.zip/download",
 * "timestamp":"20140731",
 * "codename":"find7u",
 * "md5sumdelta":"93ca0c5a1a5b75ceb4ac3382295c2464"
 * }]
 */
public class UpdateEntry {

    public String json;
    public String channel;
    public String filename;
    public String md5sum;
    public String downloadurl;
    public long timestamp;
    public String codename;
    public String md5sumdelta;

    public UpdateEntry() {
        // NOOP
    }

    public UpdateEntry(final JSONObject jsonObject) {
        json = jsonObject.toString();
        channel = getJsonString(jsonObject, "channel");
        filename = getJsonString(jsonObject, "filename");
        md5sum = getJsonString(jsonObject, "md5sum");
        downloadurl = getJsonString(jsonObject, "downloadurl");
        timestamp = Utils.tryParseLong(getJsonString(jsonObject, "timestamp"));
        codename = getJsonString(jsonObject, "codename");
        md5sumdelta = getJsonString(jsonObject, "md5sumdelta");
    }

    private String getJsonString(final JSONObject jsonObject, final String key) {
        try {
            return jsonObject.getString(key);
        } catch (JSONException jse) {
            Logger.e(this, "getJsonString", jse);
            return "unknown";
        }
    }

}
