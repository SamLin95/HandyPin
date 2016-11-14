package edu.slin77gatech.handypin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.slin77gatech.handypin.utils.LoginManager;
import edu.slin77gatech.handypin.utils.MarkerWindowAdapter;
import edu.slin77gatech.handypin.utils.NetworkSingleton;
import edu.slin77gatech.handypin.utils.PinData;
import edu.slin77gatech.handypin.utils.PinDetail;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ListView.OnItemClickListener {

    private ListView leftMenu;
    private DrawerLayout drawer;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private FloatingActionButton fab;
    private Location currentLocation;
    private LocationManager mLocationManager;

    private Marker myMarker;
    private List<Marker> otherMarkers;
    private PinData myPinData;

    private Map<Marker, PinData> markerData;

    private final long LOCATION_REFERSTH_TIME = 10L;
    private final float LOCATION_REFRESH_DISTANCE = 10f;


    public static final int SET_NEW_PIN_REQUEST = 1;
    public static final String CURRENT_LATITUDE = "currentLat";
    public static final String CURRENT_LONGITUDE = "currentLng";
    public static final String TAGS = "tags";

    private static final String GET_PINS_URL =
            "http://ec2-54-208-245-21.compute-1.amazonaws.com/api/pins?sw_longitude=%f&sw_latitude=%f&ne_longitude=%f&ne_latitude=%f";

    // Constants for left menu actions
    private static final int REFRESH_PINS =  0;
    private static final int SET_NEW_PIN = 1;
    private static final int LOGOUT = 2;

    private static final String[] MENU_TEXTS = {"refresh", "set new pin", "log out"};

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.pink_map_style));
        setUpMap();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fab = (FloatingActionButton) findViewById(R.id.myFAB);
        ((TextView)findViewById(R.id.map_activity_username)).setText(LoginManager.getInstance().getCurrentUser().getUsername());
        leftMenu = (ListView) findViewById(R.id.map_left_drawer);
        leftMenu.setAdapter(new ArrayAdapter<String>(this, R.layout.map_activity_menu_item, MENU_TEXTS));
        leftMenu.setOnItemClickListener(this);

        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);

        markerData = new HashMap<Marker, PinData>();
        otherMarkers = new ArrayList<Marker>(10);

        setUpButtons();
        setUpLocationServices();
        setUpMapIfNeeded();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_NEW_PIN_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, "Successfully set the pin!", Toast.LENGTH_SHORT).show();
            myPinData = PinData.fromIntent(data);

            //myMarker.remove();
            Marker newMarker;
            newMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(myPinData.getLatitude(), myPinData.getLongitude()))
                    .zIndex(myMarker.getZIndex() + 1)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            //Update the marker data to link new detail data to this marker.
            markerData.put(newMarker, myPinData);
            mMap.setInfoWindowAdapter(new MarkerWindowAdapter(getBaseContext(), markerData));
            myMarker.showInfoWindow();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(this);
        }
    }

    private void setUpButtons() {
        fab.setOnClickListener(new SetNewPinListener());
    }

    private void setUpLocationServices() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No GPS permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFERSTH_TIME, LOCATION_REFRESH_DISTANCE, this);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "No GPS permission", Toast.LENGTH_SHORT).show();
                animateCameraTo(0, 0);
                return;
            }
            currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (currentLocation == null) {
                animateCameraTo(0, 0);
                return;
            }

            animateCameraToAndSetOtherPins(currentLocation.getLatitude(), currentLocation.getLongitude());

            if (myMarker == null) {
                myMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
            }
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent i = new Intent(getBaseContext(), PinDetailActivity.class);
                    PinData data = markerData.get(marker);
                    if (data != null) {
                        data.putInIntent(i);
                        startActivity(i);
                    }
                }
            });
        }

    }

    private void animateCameraTo(double lat, double lng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15f));
    }

    private void animateCameraToAndSetOtherPins(double lat, double lng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15f), 30 ,new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                setOtherMarkers();
            }

            @Override
            public void onCancel() {

            }
        });
    }


    private void setOtherMarkers() {

        // remove all other markers
        LatLng sw = mMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng ne = mMap.getProjection().getVisibleRegion().latLngBounds.northeast;

        String url = String.format(GET_PINS_URL, sw.longitude, sw.latitude, ne.longitude, ne.latitude);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<PinData> datas;
                        try {
                            datas = PinData.fromJsonArray(response);

                        } catch (JSONException e) {
                            Toast.makeText(MapsActivity.this, "error parsing response", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Remove all old other markers;
                        for (Marker mk : otherMarkers) {
                            markerData.remove(mk);
                            mk.remove();
                        }
                        otherMarkers = new ArrayList<Marker>(10);
                        for (PinData data : datas) {
                            Marker newMkr = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(data.getLatitude(), data.getLongitude()))
                                    .icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            markerData.put(newMkr, data);
                            otherMarkers.add(newMkr);
                        }

                        mMap.setInfoWindowAdapter(new MarkerWindowAdapter(getBaseContext(), markerData));
                        Toast.makeText(MapsActivity.this, String.format("There are %d pins in your area", otherMarkers.size()), Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapsActivity.this, "network error", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        NetworkSingleton.getInstance(getBaseContext()).addToRequestQueue(request);
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        animateCameraTo(location.getLatitude(), location.getLongitude());
        if (myMarker != null) {
            myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case REFRESH_PINS:
                setOtherMarkers();
                drawer.closeDrawers();
                break;
            case SET_NEW_PIN:
                startSetNewPinActivity();
                drawer.closeDrawers();
                break;
            case LOGOUT:
                LoginManager.getInstance().logout(this);
                break;
            default:
                Toast.makeText(this, "Unsupported action", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSetNewPinActivity() {
        Intent intent = new Intent(getBaseContext(), SetPinActivity.class);
        intent.putExtra(CURRENT_LATITUDE, currentLocation.getLatitude())
                .putExtra(CURRENT_LONGITUDE, currentLocation.getLongitude());
        startActivityForResult(intent, SET_NEW_PIN_REQUEST);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Maps Page") // TODO: Define a title for the content shown.
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class SetNewPinListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startSetNewPinActivity();
        }
    }
}
