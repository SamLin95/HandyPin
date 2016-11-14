package edu.slin77gatech.handypin;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.slin77gatech.handypin.utils.LoginManager;
import edu.slin77gatech.handypin.utils.NetworkSingleton;
import edu.slin77gatech.handypin.utils.PinData;
import edu.slin77gatech.handypin.utils.PinDetail;
import edu.slin77gatech.handypin.utils.TagsAdapter;

public class SetPinActivity extends AppCompatActivity implements OnMapReadyCallback, TagsAdapter.OnTagClickCallBack {
    private GoogleMap mMap;
    private EditText title;
    private EditText description;
    private FloatingActionButton fab;
    private Button addTagBtn;
    private MapView mapView;
    private RecyclerView tagList;

    private LinearLayoutManager mLayoutManager;

    private HashSet<String> tags = new HashSet<>();

    private TagsAdapter adapter;

    private double curLat;
    private double curLng;

    public PinData data;

    public static final String PIN_TITLE = "pin_title";
    public static final String PIN_DESCRIPTION = "pin_description";
    public static final String PIN_TAGS = "pin_tags";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        curLat = getIntent().getDoubleExtra(MapsActivity.CURRENT_LATITUDE, 0);
        curLng = getIntent().getDoubleExtra(MapsActivity.CURRENT_LONGITUDE, 0);

        title = (EditText) findViewById(R.id.pin_title_text);
        description = (EditText) findViewById(R.id.pin_description_text);

        fab = (FloatingActionButton) findViewById(R.id.pin_fab);
        setupButton();

        mapView = (MapView) findViewById(R.id.pin_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        tagList = (RecyclerView)findViewById(R.id.pin_tags_list);
        mLayoutManager = new LinearLayoutManager(getBaseContext());
        tagList.setLayoutManager(mLayoutManager);
        adapter = new TagsAdapter(new ArrayList<String>(), this);
        tagList.setAdapter(adapter);

        addTagBtn = (Button)findViewById(R.id.set_pin_add_tag_btn);
        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addTag("");
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setupMap(double lat, double lng) {
        animateCameraTo(lat, lng);
        mMap.addMarker(new MarkerOptions().title("My location").position(new LatLng(lat, lng)));
    }

    private void setupButton() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            attempSet();
            }
        });
    }

    private void attempSet() {
        title.setError(null);
        description.setError(null);


        View focusView = null;
        boolean cancel = false;

        String titleStr = title.getText().toString();
        String descriptionStr = description.getText().toString();

        if (titleStr.isEmpty()) {
            title.setError("title can not be empty");
            focusView = title;
            cancel = true;
        }

        if (descriptionStr.isEmpty()) {
            description.setError("description can not be empty");
            focusView = description;
            cancel = true;
        }

        if (tags.size() == 0) {
            Toast.makeText(this, "You must specify at least one tag", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            PinData data = new PinData(titleStr, 0, descriptionStr);
            data.setLatitude(curLat);
            data.setLongitude(curLng);

            PinDetail pd = new PinDetail(descriptionStr, titleStr, 0);
            for (String t : tags) {
                pd.addTag(t);
            }
            pd.setUser(LoginManager.getInstance().getCurrentUser());
            data.setPinDetail(pd);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    data.getCreateNewPinURI(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Intent i = new Intent();
                    PinData newData = null;
                    try {
                        newData = PinData.fromJson(response);
                    } catch (JSONException e) {
                        Toast.makeText(SetPinActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    newData.putInIntent(i);
                    setResult(MapsActivity.RESULT_OK, i);
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SetPinActivity.this, "error in request", Toast.LENGTH_SHORT).show();
                }
            });
            NetworkSingleton.getInstance(getBaseContext()).addToRequestQueue(request);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.pink_map_style));
        setupMap(curLat, curLng);
    }

    private void animateCameraTo(double lat, double lng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15f));
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SetPin Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


    @Override
    public void onTagCheck(String tagString) {
        tags.add(tagString);
    }

    @Override
    public void onTagUnCheck(String tagString) {
        tags.remove(tagString);
    }

}
