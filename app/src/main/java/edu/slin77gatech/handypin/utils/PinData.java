package edu.slin77gatech.handypin.utils;

import android.content.Intent;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slin on 10/16/16.
 * pin summary data for displaying on marker.
 */

public class PinData {

    private static final String NAME_PREFIX = "PIN_DATA_";

    public static final String ID_FIELD = "id";
    public static final String TITLE_FIELD = "title";
    public static final String SCORE_FIELD = "vote_score";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String GEO_FIELD = "geo";
    public static final String LATITUDE_FIELD = "lat";
    public static final String LONGITUTUDE_FIELD = "lng";

    public static final String ID_TAG = NAME_PREFIX + ID_FIELD;
    public static final String TITLE_TAG = NAME_PREFIX + TITLE_FIELD;
    public static final String SCORE_TAG = NAME_PREFIX + SCORE_FIELD;
    public static final String DESCRIPTION_TAG = NAME_PREFIX + DESCRIPTION_FIELD;
    public static final String LATITUDE_TAG = NAME_PREFIX + LATITUDE_FIELD;
    public static final String LONGITUTUDE_TAG = NAME_PREFIX + LONGITUTUDE_FIELD;

    private String title;
    private String short_title;
    private int rating;
    private String description;
    private double latitude;
    private double longitude;
    private int id;

    private PinDetail pinDetail;

    public static PinData fromJson(JSONObject data) throws JSONException {
        PinData toRet = new PinData(data.getString(TITLE_FIELD),
                data.getInt(SCORE_FIELD), data.getString(DESCRIPTION_FIELD));
        toRet.setLatitude(data.getJSONObject(GEO_FIELD).getDouble(LATITUDE_FIELD));
        toRet.setLongitude(data.getJSONObject(GEO_FIELD).getDouble(LONGITUTUDE_FIELD));
        toRet.setId(data.getInt(ID_FIELD));
        toRet.setPinDetail(PinDetail.fromJsonObj(data));
        return toRet;
    }

    public static List<PinData> fromJsonArray(JSONArray data) throws JSONException {
        List<PinData> ret = new ArrayList<>();
        for (int i = 0 ; i < data.length(); i++) {
            ret.add(fromJson(data.getJSONObject(i)));
        }
        return ret;
    }

    public static PinData fromIntent(Intent data) {
        PinData toRet = new PinData(data.getStringExtra(TITLE_TAG),
                data.getIntExtra(SCORE_TAG, 0), data.getStringExtra(DESCRIPTION_TAG));
        toRet.setLatitude(data.getDoubleExtra(LATITUDE_TAG, 0));
        toRet.setLongitude(data.getDoubleExtra(LONGITUTUDE_TAG, 0));
        toRet.setId(data.getIntExtra(ID_TAG, 0));
        toRet.setPinDetail(PinDetail.fromIntent(data));
        return toRet;
    }

    public Intent putInIntent(Intent data) {
        data.putExtra(TITLE_TAG, title)
                .putExtra(SCORE_TAG, rating)
                .putExtra(DESCRIPTION_TAG, description)
                .putExtra(LATITUDE_TAG, latitude)
                .putExtra(ID_TAG, id)
                .putExtra(LONGITUTUDE_TAG, longitude);
        return pinDetail.putInIntent(data);
    }

    public PinData(String title, int rating, String description) {
        this.title = title.substring(Math.min(title.length(), 50));
        this.short_title = title.substring(Math.min(title.length(), 20));
        this.rating = rating;
        this.description = description;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public PinDetail getPinDetail() {
        return pinDetail;
    }

    public void setPinDetail(PinDetail pinDetail) {
        this.pinDetail = pinDetail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreateNewPinURI() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("ec2-54-208-245-21.compute-1.amazonaws.com")
                .appendPath("api")
                .appendPath("pins")
                .appendQueryParameter("title", title)
                .appendQueryParameter("short_title", short_title)
                .appendQueryParameter("description", description)
                .appendQueryParameter("owner_id", String.valueOf(pinDetail.getUser().id))
                .appendQueryParameter("longitude", String.valueOf(longitude))
                .appendQueryParameter("latitude", String.valueOf(latitude));
        for (String t : pinDetail.getTags()) {
            builder.appendQueryParameter("tag_strings", t);
        }
        return builder.build().toString();
    }
}
