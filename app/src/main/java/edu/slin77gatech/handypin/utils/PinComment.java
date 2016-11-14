package edu.slin77gatech.handypin.utils;

import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by slin on 10/16/16.
 */

public class PinComment {

    public static final String COMMENT_CONTENT_FIELD = "content";
    public static final String COMMENT_TIMESTAMP_FIELD = "created_at";
    public static final String COMMENT_OWNER_FIELD = "owner";

    private String id;
    private String content;
    private String username;
    private String timestamp;

    public static List<PinComment> listFromIntent(Intent intent) {
        String[] contents = intent.getStringArrayExtra(PinDetail.DETAIL_COMMENTS_STRINGS);
        String[] timestamps = intent.getStringArrayExtra(PinDetail.DETAIL_COMMENTS_TIMES);
        String[] usernames = intent.getStringArrayExtra(PinDetail.DETAIL_COMMENTS_USER_IDS);

        ArrayList<PinComment> ret = new ArrayList<>(5);

        if (contents == null || timestamps == null || usernames == null) {
            return ret;
        }

        if (contents.length != timestamps.length || timestamps.length != usernames.length) {
            throw new IllegalStateException("Can not decode PinComments from intent");
        }

        for (int i = 0; i < contents.length; i++) {
            ret.add(new PinComment(contents[i], usernames[i], timestamps[i]));
        }

        return ret;
    }

    public static PinComment fromJson(JSONObject data) throws JSONException{
        return new PinComment(data.getString(COMMENT_CONTENT_FIELD),
                data.getJSONObject(COMMENT_OWNER_FIELD).getString(User.USER_USERNAME_FIELD),
                data.getString(COMMENT_TIMESTAMP_FIELD));
    }

    public static List<PinComment> listFromJSONArray(JSONArray data) throws JSONException {
        List<PinComment> ret = new ArrayList<>(5);
        for (int i = 0 ; i < data.length() ; i++) {
            ret.add(fromJson(data.getJSONObject(i)));
        }
        return ret;
    }

    public PinComment(String content, String username, String timestamp) {
        this.content = content;
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
