package edu.slin77gatech.handypin.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import edu.slin77gatech.handypin.R;

/**
 * Created by slin on 10/24/16.
 */

public class MarkerWindowAdapter implements GoogleMap.InfoWindowAdapter {

    public MarkerWindowAdapter(Context ctx, Map<Marker, PinData> pinDataMap) {
        this.ctx = ctx;
        this.pinDataMap = pinDataMap;
    }

    private Map<Marker, PinData> pinDataMap;
    private Context ctx;

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        PinData data = pinDataMap.get(marker);
        if (data == null) {
            return null;
        } else {
            View v = LayoutInflater.from(ctx).inflate(R.layout.marker_info_window, null);
            //set necessary fields
            TextView title = (TextView) v.findViewById(R.id.my_marker_title);
            title.setText(data.getTitle());

            TextView descripion = (TextView)v.findViewById(R.id.my_marker_description);
            descripion.setText(data.getDescription());

            TextView rate = (TextView)v.findViewById(R.id.my_marker_rate);
            rate.setText("Vote: " + data.getRating());
            return v;
        }
    }

}
