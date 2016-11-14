package edu.slin77gatech.handypin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import edu.slin77gatech.handypin.LoginActivity;
import edu.slin77gatech.handypin.MapsActivity;
import edu.slin77gatech.handypin.R;

/**
 * Created by slin on 10/30/16.
 */

public class LoginManager {

    public interface LoginCallBack {
        void loginSuccess(User user);
        void loginFail(String errorMsg);
    }

    private static LoginManager instance = null;
    private boolean isLogin = false;
    private User currentUser;
    private static final String URL =
            "http://ec2-54-208-245-21.compute-1.amazonaws.com/auth/signin?username=%s&password=%s";

    public static LoginManager getInstance() {
        if (instance == null) instance = new LoginManager();
        return instance;
    }

    private LoginManager() {}

    public void doLogin(String username, String password, final LoginCallBack callback, Context ctx) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                String.format(URL, username, password),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        User user = null;
                        try {
                            user = User.fromJson(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.loginFail("Invalid response data, please try again.");
                            return;
                        }
                        currentUser = user;
                        isLogin = true;
                        callback.loginSuccess(user);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.loginFail(error.getMessage());
            }
        });
        NetworkSingleton.getInstance(ctx).addToRequestQueue(request);
    }

    public @Nullable User getCurrentUser() {
        if (isLogin) return currentUser;
        return null;
    }

    public void logout(Activity currentActivity) {
        currentUser = null;
        isLogin = false;
        Intent i = new Intent(currentActivity, LoginActivity.class);
        currentActivity.startActivity(i);
        currentActivity.finish();
    }


}
