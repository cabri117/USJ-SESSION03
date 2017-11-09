package com.example.hawk.imhungry;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.parceler.Parcels;

import java.util.List;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);




    }

    @Override
    protected void onStart() {
        super.onStart();
        String requiredPermission = "android.permission.ACCESS_FINE_LOCATION";
        int checkVal =this.checkCallingOrSelfPermission(requiredPermission);
        if (checkVal==PackageManager.PERMISSION_GRANTED){
            onInit();
        } else {
            checkGPSPermission();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem register = menu.findItem(R.id.location);
        register.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.maps:
                Intent i = new Intent(this,MapsActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkGPSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        } else {
            return true;
        }
    }

    public void onInit() {
        checkGPSPermission();
        double lat = 0.0;
        double log = 0.0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location != null) {
            lat = location.getLatitude();
            log = location.getLongitude();
        }
        listView = findViewById(R.id.listView);
        JSONResourceReader reader = new JSONResourceReader(this.getResources(), R.raw.restaurants);
        final List<Restaurant> jsonObj = reader.constructUsingGson();
        ListAdapter la = new ListAdapter(this,R.layout.adapter_list,jsonObj,
                lat, log);
        listView.setAdapter(la);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Parcelable listParcelable = Parcels.wrap(jsonObj.get(position));
                Intent i = new Intent(getApplicationContext(),RestaurantsDetailsActivity.class);
                i.putExtra("RESTAURANT", listParcelable);
                startActivity(i);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    onInit();

                } else {
                   checkGPSPermission();
                }
            }
        }
    }
}
