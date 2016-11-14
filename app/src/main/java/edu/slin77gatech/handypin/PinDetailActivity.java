package edu.slin77gatech.handypin;

import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

import edu.slin77gatech.handypin.utils.CommentsAdapter;
import edu.slin77gatech.handypin.utils.LoginManager;
import edu.slin77gatech.handypin.utils.NetworkSingleton;
import edu.slin77gatech.handypin.utils.PinComment;
import edu.slin77gatech.handypin.utils.PinData;
import edu.slin77gatech.handypin.utils.PinDetail;
import edu.slin77gatech.handypin.utils.User;
import layout.CommentsFragment;
import layout.VoteFragment;

public class PinDetailActivity extends AppCompatActivity implements CommentsFragment.CommentsFragmentInteractionListerner,
        VoteFragment.VoteInteractionListener, User.OnDetailReadyCallBack {

    TextView mAuthorName;
    TextView mAuthorEmail;
    ImageButton mContactAutorBtn;

    TextView mPinTitle;
    TextView mPinContent;
    TextView mPinRate;

    ImageButton getCommentsBtn;

    CardView mVoteCard;
    private PinData pinData;
    private PinDetail detail;

    boolean upVoted = false;
    boolean downVoted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_detail);

        //Get detail data received from Intent.
        pinData = PinData.fromIntent(getIntent());
        detail = pinData.getPinDetail();

        populateData();

        getCommentsBtn = (ImageButton) findViewById(R.id.get_comments_btn);
        getCommentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCommentsAndShowFragment();
            }
        });


        mPinRate = (TextView) findViewById(R.id.pin_detail_rate);
        mPinRate.setText(pinData.getRating() + "");
        mPinRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoteFragment();
            }
        });

        mVoteCard = (CardView) findViewById(R.id.vote_card);
        mVoteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoteFragment();
            }
        });

        mAuthorName = (TextView)findViewById(R.id.detail_author_name);
        mAuthorName.setText(pinData.getPinDetail().getUser().getUsername());

        mAuthorEmail = (TextView)findViewById(R.id.detail_author_email);

        detail.getUser().loadDetailInfoAsync(this, this);
    }

    private void loadCommentsAndShowFragment() {
        final String url = String.format("http://ec2-54-208-245-21.compute-1.amazonaws.com/api/pins/%d", pinData.getId());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<PinComment> newComments;
                try {
                    newComments = PinComment.listFromJSONArray(response.getJSONArray(PinDetail.COMMENTS_FIELD));
                } catch (JSONException e) {
                    Toast.makeText(PinDetailActivity.this, "Error parsing response, please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // update pin detail comments.
                pinData.getPinDetail().setComments(newComments);
                showCommentsFragment(newComments);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PinDetailActivity.this,
                        "Error occurs when try to fetch comments from internet, so comments may not be updated",
                        Toast.LENGTH_SHORT).show();
                showCommentsFragment(pinData.getPinDetail().getComments());
            }
        });
        NetworkSingleton.getInstance(getBaseContext()).addToRequestQueue(request);
    }

    private void showCommentsFragment(List<PinComment> pinComments) {
        FragmentManager fragmentManager = getFragmentManager();

        String[] authors = new String[pinComments.size()];
        String[] contents = new String[pinComments.size()];
        String[] timestamps = new String[pinComments.size()];

        for (int i = 0; i < pinComments.size(); i++) {
            authors[i] = pinComments.get(i).getUsername();
            contents[i] = pinComments.get(i).getContent();
            timestamps[i] = pinComments.get(i).getTimestamp();
        }

        CommentsFragment fragment = CommentsFragment.newInstance(authors,
                contents, timestamps);

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, CommentsFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void showVoteFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        VoteFragment fragment = VoteFragment.newInstance(pinData.getRating());

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, fragment, VoteFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void populateData() {
        mPinTitle = (TextView) findViewById(R.id.pin_detail_title);
        mPinTitle.setText(detail.getTitle());

        mPinContent = (TextView) findViewById(R.id.pin_detail_content);
        mPinContent.setText(detail.getDescription());

        mPinRate = (TextView) findViewById(R.id.pin_detail_rate);
        mPinRate.setText(String.valueOf(detail.getRating()));
    }

    @Override
    public void sendNewComment(String comment) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("ec2-54-208-245-21.compute-1.amazonaws.com")
                .appendPath("api")
                .appendPath("comment")
                .appendQueryParameter("pin_id", String.valueOf(pinData.getId()))
                .appendQueryParameter("owner_id", String.valueOf(LoginManager
                        .getInstance().getCurrentUser().getId()))
                .appendQueryParameter("content", comment);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                builder.toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(PinDetailActivity.this,
                                "new comment sent!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PinDetailActivity.this, "Error occurs when tried to comment. Please try again!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        NetworkSingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public void upvote() {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("http")
                .authority("ec2-54-208-245-21.compute-1.amazonaws.com")
                .appendPath("api")
                .appendPath("votes")
                .appendQueryParameter("user_id", String.valueOf(LoginManager.getInstance().getCurrentUser().getId()))
                .appendQueryParameter("pin_id", String.valueOf(pinData.getId()))
                .appendQueryParameter("vote", "1");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                builder.build().toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(PinDetailActivity.this, "You have successfully up vote!",
                                Toast.LENGTH_SHORT).show();
                        pinData.setRating(pinData.getRating() + 1);
                        mPinRate.setText(String.valueOf(pinData.getRating()));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PinDetailActivity.this, "Error occurs, you can not vote twice.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        NetworkSingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public void downvote() {
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("http")
                .authority("ec2-54-208-245-21.compute-1.amazonaws.com")
                .appendPath("api")
                .appendPath("vote")
                .appendQueryParameter("user_id", String.valueOf(LoginManager.getInstance().getCurrentUser().getId()))
                .appendQueryParameter("pin_id", String.valueOf(pinData.getId()))
                .appendQueryParameter("vote", "0");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                builder.build().toString(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(PinDetailActivity.this, "You have successfully down vote!",
                                Toast.LENGTH_SHORT).show();
                        pinData.setRating(pinData.getRating() - 1);
                        mPinRate.setText(String.valueOf(pinData.getRating()));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PinDetailActivity.this, "Error occurs, you can not vote twice.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        NetworkSingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public void detailSuccess(User user) {
        if (mAuthorEmail != null) {
            mAuthorEmail.setText(user.getEmail());
        }
    }

    @Override
    public void detailFail(String error) {
        Toast.makeText(this, error , Toast.LENGTH_SHORT).show();
    }
}

