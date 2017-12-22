package com.example.hawk.imhungry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static com.example.hawk.imhungry.AppHelpers.meterDistanceBetweenPoints;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, JsonFromInternet.MyAsyncTaskListener {


    TextView restText;
    TextView textKm;
    GoogleMap mMap;
    Polyline linePoly;
    RelativeLayout restLay;
    Button infoBtn;
    JsonFromInternet jFI;

    private Restaurant mRestaurant;
    private List<Restaurant> mRestaurants;
    double lat = 0.0;
    double log = 0.0;
    LatLng mCurrentUserMarkerClicked;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        restText = findViewById(R.id.restText);
        restLay = findViewById(R.id.restLay);
        textKm = findViewById(R.id.textKm);
        infoBtn = findViewById(R.id.infoBtn);

        mProgressDialog = new ProgressDialog(this);

        mRestaurant = Parcels.unwrap(getIntent().getParcelableExtra("RESTAURANT_LOCATION"));
        lat = getIntent().getDoubleExtra("actualLat", 0.0);
        log = getIntent().getDoubleExtra("actualLog", 0.0);
        Log.d("MAPS", String.valueOf(lat));
        Log.d("MAPS", String.valueOf(log));

        if (mRestaurant != null) {
            setTitle(mRestaurant.getName());
        } else {
            mRestaurants = Parcels.unwrap(getIntent().getParcelableExtra("RESTAURANT_LIST"));
            setTitle(getString(R.string.title_activity_maps));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jFI != null) {
            if (!jFI.isCancelled()) {
                jFI.cancel(true);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setOnMarkerClickListener(this);
        int count = 0;
        LatLng location;

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        location = new LatLng(lat, log);
        mMap.addMarker(addMarker(location, getString(R.string.my_location),
                "-1", mMap, R.drawable.my_marker));
        builder.include(location);

        if (mRestaurant != null) {
            // Add a marker in selected restaurant and move the camera
            location = new LatLng(mRestaurant.getLatitude(), mRestaurant.getLongitude());

            mMap.addMarker(addMarker(location, mRestaurant.name,
                    String.valueOf(count), mMap, R.drawable.rest_marker));
            builder.include(location);
        } else {
            for (Restaurant restaurant : mRestaurants) {
                location = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                mMap.addMarker(addMarker(location, restaurant.name,
                        String.valueOf(count), mMap, R.drawable.rest_marker));

                builder.include(location);
                count = count + 1;
            }
        }

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                double width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width * 0.20);
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public MarkerOptions addMarker(LatLng location, String name, String position, GoogleMap mMap,
                                   int icon) {

        MarkerOptions marker = new MarkerOptions()
                .position(location)
                .title(name)
                .snippet(position).icon(BitmapDescriptorFactory.fromResource(icon));
        mMap.addMarker(marker);

        return marker;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (mCurrentUserMarkerClicked != null &&
                mCurrentUserMarkerClicked.latitude == marker.getPosition().latitude &&
                mCurrentUserMarkerClicked.longitude == marker.getPosition().longitude)
            return true;
        else mCurrentUserMarkerClicked = marker.getPosition();

        restLay.setVisibility(View.GONE);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(lat, log));
        builder.include(marker.getPosition());

        if (marker.getTitle().equalsIgnoreCase(getString(R.string.my_location))) {
            marker.showInfoWindow();
            return true;
        }

        int position = Integer.parseInt(marker.getSnippet());

        String urlTopass;
        if (mRestaurant != null) {
            setAttrs(builder, position, marker, mRestaurant, false);
            urlTopass = URLContants.makeRoute(lat, log, mRestaurant.getLatitude(), mRestaurant.getLongitude());
        } else {
            setAttrs(builder, position, marker, mRestaurants.get(position), true);
            urlTopass = URLContants.makeRoute(lat, log, mRestaurants.get(position).getLatitude(), mRestaurants.get(position).getLongitude());
        }

        jFI = new JsonFromInternet.Builder(urlTopass).build();
        jFI.setListener(this);
        jFI.execute();

        return true;
    }

    public void setAttrs(LatLngBounds.Builder builder, final int position, Marker marker, final Restaurant restaurant, boolean showInfoButton) {
        double width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.20);
        double distance = meterDistanceBetweenPoints(lat, log,
                restaurant.getLatitude(),
                restaurant.getLongitude());
        builder.include(marker.getPosition());
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        //mMap.moveCamera(cu);
        mMap.animateCamera(cu);
        restText.setText(restaurant.getName());
        textKm.setText(String.format("%s KM",
                String.format("%.2f", distance * 0.001)));

        restLay.setVisibility(View.VISIBLE);

        if (showInfoButton)
            infoBtn.setVisibility(View.VISIBLE);
        else infoBtn.setVisibility(View.GONE);

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parcelable listParcelable = Parcels.wrap(restaurant);
                Intent i = new Intent(getApplicationContext(), RestaurantDetailsActivity.class);
                i.putExtra("RESTAURANT", listParcelable);
                i.putExtra("actualLog", log);
                i.putExtra("actualLat", lat);
                startActivity(i);
            }
        });
    }

    @Override
    public void onPreExecuteConcluded() {
        mProgressDialog.setMessage(getString(R.string.fetching_route_please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    @Override
    public void onPostExecuteConcluded(String result) {
        mProgressDialog.hide();
        if (result != null) {
            drawPath(result);
        }
    }

    public void drawPath(String result) {
        try {
            // Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            PolylineOptions options = new PolylineOptions().width(10).color(Color.RED).geodesic(true);
            options.addAll(list);

            if (linePoly != null) {
                linePoly.remove();
            }

            linePoly = mMap.addPolyline(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
