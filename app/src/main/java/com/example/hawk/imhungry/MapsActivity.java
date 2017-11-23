package com.example.hawk.imhungry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.parceler.Parcels;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Restaurant mRestaurant;
    private List<Restaurant> mRestaurants;

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

        mRestaurant = Parcels.unwrap(getIntent().getParcelableExtra("RESTAURANT_LOCATION"));

        if (mRestaurant != null) {
            setTitle(mRestaurant.getName());
        } else {
            mRestaurants = Parcels.unwrap(getIntent().getParcelableExtra("RESTAURANT_LIST"));
            setTitle(getString(R.string.title_activity_maps));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        GoogleMap mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        if (mRestaurant != null) {
            // Add a marker in selected restaurant and move the camera
            LatLng location = new LatLng(mRestaurant.getLatitude(), mRestaurant.getLongitude());
            MarkerOptions marker = new MarkerOptions()
                    .position(location)
                    .title(mRestaurant.name)
                    .snippet("Tel: " + mRestaurant.phone);
            mMap.addMarker(marker);

            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17.0f));
        } else {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (Restaurant restaurant : mRestaurants) {
                LatLng location = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
                MarkerOptions marker = new MarkerOptions()
                        .position(location)
                        .title(restaurant.name)
                        .snippet("Tel: " + restaurant.phone);
                mMap.addMarker(marker);

                //the include method will calculate the min and max bound.
                builder.include(marker.getPosition());
            }

            // Global Zoom
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);
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
}
