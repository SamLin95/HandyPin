package edu.slin77gatech.handypin.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by slin on 10/16/16.
 */

public class User {
    public String getUsername() {
        return username;
    }

    public String getNickName() {
        return nickName;
    }

    public int getId() {
        return id;
    }

    public interface OnDetailReadyCallBack {
        void detailSuccess(User user);
        void detailFail(String error);
    }


    private static String DETAIL_INFO_URL =
            "http://ec2-54-208-245-21.compute-1.amazonaws.com/api/users/%d";

    private static String NAMESPACE = "user_";

    public static final String USER_ID_FIELD = "id";
    public static final String USER_NICKNAME_FIELD = "nickname";
    public static final String USER_USERNAME_FIELD = "username";

    public static final String USER_ID_TAG = NAMESPACE + USER_ID_FIELD;
    public static final String USER_NICKNAME_TAG = NAMESPACE + USER_NICKNAME_FIELD;
    public static final String USER_USERNAME_TAG = NAMESPACE + USER_USERNAME_FIELD;

    //Basic User Info;
    String username;
    String nickName;
    int id;

    //Detail Infos.
    boolean detailAvailable = false;
    String email;
    String profileImageUrl;

    static User fromJson(JSONObject json) throws JSONException {
        return new User(json.getInt(USER_ID_FIELD),
                json.getString(USER_NICKNAME_FIELD),
                json.getString(USER_USERNAME_FIELD));
    }

    /**
     * Instantiate a basic user from intent that have user info within.
     * @param intent
     * @return
     */
    public static User fromIntent(Intent intent) {
        return new User(intent.getIntExtra(USER_ID_TAG, -1), intent.getStringExtra(USER_NICKNAME_TAG), intent.getStringExtra(USER_NICKNAME_TAG));
    }

    /**
     * Encode basic User info from intent that has user info within.
     * @param intent
     * @return
     */
    public Intent encodeInIntent(Intent intent) {
        intent.putExtra(USER_ID_TAG, id);
        intent.putExtra(USER_NICKNAME_TAG, nickName);
        intent.putExtra(USER_USERNAME_TAG, username);
        return intent;
    }

    public User(int id, String nickName, String username) {
//        if (id < 0 || nickName == null || username == null) {
//            throw new IllegalArgumentException("Id, nickname, and username can not be null");
//        }
        this.id = id;
        this.nickName = nickName;
        this.username = username;
    }

    public void loadDetailInfoAsync(Context ctx, final OnDetailReadyCallBack callBack) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                String.format(DETAIL_INFO_URL, id),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            email = response.getString("email");
                            //profileImageUrl = response.getJSONObject("profile_photo").getString("download_url");
                            detailAvailable = true;
                            callBack.detailSuccess(User.this);
                        } catch (JSONException e) {
                            callBack.detailFail("Can not parse Json");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        NetworkSingleton.getInstance(ctx).addToRequestQueue(request);
    }

    public @Nullable String getEmail() {
        if (detailAvailable) return email;
        return null;
    }

    public @Nullable String getProfileImageUrl() {
        if (detailAvailable) return profileImageUrl;
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
