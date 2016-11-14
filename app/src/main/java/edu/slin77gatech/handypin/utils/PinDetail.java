package edu.slin77gatech.handypin.utils;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by slin on 10/16/16.
 */

public class PinDetail {
    // For intents
    public static final String DETAIL_TITLE = "d_title";
    public static final String DETAIL_DESCRIPTION = "d_description";
    public static final String DETAIL_RATING = "d_rating";

    // For intent comments
    public static final String DETAIL_COMMENTS_STRINGS = "d_comments_STRINGS";
    public static final String DETAIL_COMMENTS_TIMES = "d_comment_times";
    public static final String DETAIL_COMMENTS_USER_IDS = "d_comments_user_ids";

    // Tags for Json
    public static final String TITLE_FIELD = "title";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String RATE_FIELD = "vote_score";
    public static final String TIMESTAMP_FIELD = "created_at";
    public static final String OWNER_FIELD = "owner";
    public static final String COMMENTS_FIELD = "comments";

    private String title;
    private String description;
    private String timestamp;
    private int rating;

    //User associate with this comments;
    private User user;

    private List<String> tags;
    private List<PinComment> comments;


    public int getRating() {
        return rating;
    }

    public static PinDetail fromIntent(Intent intent) {
        PinDetail ret = new PinDetail(intent.getStringExtra(DETAIL_DESCRIPTION),
                intent.getStringExtra(DETAIL_TITLE), intent.getIntExtra(DETAIL_RATING, 0));
        ret.setUser(User.fromIntent(intent));
        ret.setComments(PinComment.listFromIntent(intent));
        return ret;
    }

    public Intent putInIntent(Intent intent) {
        intent.putExtra(DETAIL_TITLE, title)
                .putExtra(DETAIL_DESCRIPTION, description)
                .putExtra(DETAIL_RATING, rating);
        //encode comments data into intent as well.
        if (comments.size() != 0) {
            String[] commentStrings = new String[comments.size()];
            String[] commentUserIds = new String[comments.size()];
            String[] commentTimes = new String[comments.size()];
            for (int i = 0 ; i < comments.size(); i++) {
                commentStrings[i] = comments.get(i).getContent();
                commentUserIds[i] = comments.get(i).getUsername();
                commentTimes[i] = comments.get(i).getTimestamp();
            }
            intent.putExtra(DETAIL_COMMENTS_STRINGS, commentStrings)
                    .putExtra(DETAIL_COMMENTS_TIMES, commentTimes)
                    .putExtra(DETAIL_COMMENTS_USER_IDS, commentUserIds);
        }
        return user.encodeInIntent(intent);
    }


    public static PinDetail fromJsonObj(JSONObject data) throws JSONException  {
        PinDetail pd = new PinDetail(data.getString(DESCRIPTION_FIELD), data.getString(TITLE_FIELD), data.getInt(RATE_FIELD));
        pd.setUser(User.fromJson(data.getJSONObject(OWNER_FIELD)));
        pd.setComments(PinComment.listFromJSONArray(data.getJSONArray(COMMENTS_FIELD)));
        pd.setTimestamp(data.getString(TIMESTAMP_FIELD));
        return pd;
    }

    public PinDetail(String description, String title, int rating) {
        this.description = description;
        this.title = title;
        this.comments = new ArrayList<PinComment>(5);
        this.tags = new ArrayList<String>(5);
        this.rating = rating;
    }

    public PinData getPinData() {
        return new PinData(title, rating, description);
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {

        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setComments(List<PinComment> comments) {
        this.comments = comments;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void addTags(String[] tags) {
        for (String t : tags) this.tags.add(t);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public List<String> getTags() {
        return tags;
    }

    public List<PinComment> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "description: " + description.substring(0, Math.min(description.length(), 40))
                + "......\non: " + timestamp;
    }
}
