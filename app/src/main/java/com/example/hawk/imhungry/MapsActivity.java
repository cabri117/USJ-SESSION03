package com.example.hawk.imhungry;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import org.parceler.Parcels;

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
        int count = 0;
        LatLng location = new LatLng(lat, log);



        if (mRestaurant != null) {
            // Add a marker in selected restaurant and move the camera
            location = new LatLng(mRestaurant.getLatitude(), mRestaurant.getLongitude());
           /* MarkerOptions marker = new MarkerOptions()
                    .position(location)
                    .title(mRestaurant.name)
                    .snippet("Tel: " + mRestaurant.phone);
            mMap.addMarker(marker);*/
            mMap.addMarker(addMarker(location, mRestaurant.name ,
                    "Tel: " + mRestaurant.phone, mMap, R.drawable.rest_marker));
            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f));
        } else {
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            location = new LatLng(lat, log);
            mMap.setOnMarkerClickListener(this);
            mMap.addMarker(addMarker(location, "Aqui Estoy Yo" ,
                    "0", mMap, R.drawable.my_marker));
            mMap.addMarker(addMarker(location, "Aqui Estoy Yo" ,
                    String.valueOf(mRestaurants.size()), mMap, R.drawable.my_marker));
            builder.include(location);
            for (Restaurant restaurant : mRestaurants) {
                location = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                mMap.addMarker(addMarker(location, restaurant.name ,
                        String.valueOf(count), mMap, R.drawable.rest_marker));
                /*MarkerOptions marker = new MarkerOptions()
                        .position(location)
                        .title(restaurant.name)
                        .snippet("Tel: " + restaurant.phone);
                mMap.addMarker(marker);*/

                //the include method will calculate the min and max bound.

                /*PolylineOptions line=
                        new PolylineOptions().add(new LatLng(restaurant.getLatitude(),
                                        restaurant.getLongitude()), new LatLng(lat,log))
                                .width(5).color(Color.RED);*/
                //mMap.addPolyline(line);
                builder.include(location);
                count = count + 1;


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

            // Global Zoom
            /*LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);*/
        }
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

        return  marker;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        restLay.setVisibility(View.GONE);
        if (mRestaurants != null) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(lat,log));
            builder.include(marker.getPosition());

            if(linePoly != null) {
                linePoly.remove();
            }
            PolylineOptions line= new PolylineOptions().add(marker.getPosition(),
                    new LatLng(lat,log))
                            .width(5).color(Color.RED);
            linePoly = mMap.addPolyline(line);

                int position = Integer.parseInt(marker.getSnippet());
                if(position != mRestaurants.size()) {
                    setAttrs(builder,position,marker);
                } else {
                    restLay.setVisibility(View.GONE);
                }
        } else {
            marker.showInfoWindow();
        }

        return  true;
    }

    public void setAttrs(LatLngBounds.Builder builder, final int position, Marker marker ) {
        double width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width * 0.20);
        double distance = meterDistanceBetweenPoints(lat,log,
                mRestaurants.get(position).getLatitude(),
                mRestaurants.get(position).getLongitude());
        builder.include(marker.getPosition());
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        //mMap.moveCamera(cu);
        mMap.animateCamera(cu);
        restText.setText(mRestaurants.get(position).getName());
        textKm.setText(String.format("%s KM",
                String.format("%.2f", distance * 0.001)));
        restLay.setVisibility(View.VISIBLE);



        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parcelable listParcelable = Parcels.wrap(mRestaurants.get(position));
                Intent i = new Intent(getApplicationContext(), RestaurantDetailsActivity.class);
                i.putExtra("RESTAURANT", listParcelable);
                i.putExtra("actualLog", log);
                i.putExtra("actualLat", lat);
                startActivity(i);
            }
        });
    }
}
