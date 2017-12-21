package com.example.hawk.imhungry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.hawk.imhungry.utils.meterDistanceBetweenPoints;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private Restaurant mRestaurant;
    private List<Restaurant> mRestaurants;
    double lat = 0.0;
    double log = 0.0;
    TextView restText;
    TextView textKm;
    GoogleMap mMap;
    Polyline linePoly;
    RelativeLayout restLay;
    Button infoBtn;

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
        restLay.setVisibility(View.GONE);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(new LatLng(lat, log));
        builder.include(marker.getPosition());

        if (marker.getTitle().equalsIgnoreCase(getString(R.string.my_location)))
        {
            marker.showInfoWindow();
            return true;
        }

        int position = Integer.parseInt(marker.getSnippet());

        String urlTopass;
        if (mRestaurant != null) {
            setAttrs(builder, position, marker, mRestaurant, false);
            urlTopass = makeURL(lat, log, mRestaurant.getLatitude(), mRestaurant.getLongitude());
        } else {
            setAttrs(builder, position, marker, mRestaurants.get(position), true);
            urlTopass = makeURL(lat, log, mRestaurants.get(position).getLatitude(), mRestaurants.get(position).getLongitude());
        }

        if (!TextUtils.isEmpty(urlTopass))
            new connectAsyncTask(urlTopass).execute();
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

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage(getString(R.string.fetching_route_please_wait));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if (result != null) {
                drawPath(result);
            }
        }
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=walking&alternatives=true");
        return urlString.toString();
    }

    public class JSONParser {

        InputStream is = null;
        JSONObject jObj = null;
        String json = "";

        // constructor
        public JSONParser() {
        }

        public String getJSONFromUrl(String url) {
            // Making HTTP request
            try {
                HttpURLConnection connection = null;
                URL urls = new URL(url);
                connection = (HttpURLConnection) urls.openConnection();
                connection.connect();

                is = connection.getInputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }
            return json;

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
