package com.example.hawk.imhungry;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ListActivity extends AppCompatActivity implements JsonFromInternet.MyAsyncTaskListener,
        SearchView.OnQueryTextListener{
    private ListView listView;
    private List<Restaurant> jsonString;
    private List<Restaurant> filteredList;
    MaterialProgressBar progressBar;
    JsonFromInternet jFI;
    SearchView searchView;
    double lat = 0.0;
    double log = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        progressBar = findViewById(R.id.pb);
        listView = findViewById(R.id.listView);
        jFI = new JsonFromInternet();
        jFI.setListener(this);
        String requiredPermission = "android.permission.ACCESS_FINE_LOCATION";
        int checkVal = this.checkCallingOrSelfPermission(requiredPermission);
        if (checkVal == PackageManager.PERMISSION_GRANTED) {
            checkInternetConnectivity();
        } else {
            checkGPSPermission();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!jFI.isCancelled()) {
            jFI.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)  menu.findItem(R.id.search).getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(this);
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
                if(jsonString != null) {
                    Intent i = new Intent(this, MapsActivity.class);
                    Parcelable listParcelable = Parcels.wrap(jsonString);
                    i.putExtra("RESTAURANT_LIST", listParcelable);
                    startActivity(i);
                } else {
                    Toast.makeText(this,getString(R.string.c_i_i),Toast.LENGTH_SHORT).show();
                }

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

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            lat = location.getLatitude();
            log = location.getLongitude();

        }

        setList(lat,log, jsonString);

        //JSONResourceReader reader = new JSONResourceReader(this.getResources(), R.raw.restaurants);
        //final List<Restaurant> jsonObj = jsonString;


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
                    jFI.execute();
                } else {
                    checkGPSPermission();
                }
            }
        }
    }

    @Override
    public void onPreExecuteConcluded() {
        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPostExecuteConcluded(List<Restaurant> result) {

        jsonString = result;
        onInit();
    }

    private void checkInternetConnectivity() {
        Single<Boolean> single = ReactiveNetwork.checkInternetConnectivity();
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isConnectedToInternet) throws Exception {
                        if (isConnectedToInternet) {
                            jFI.execute();
                        } else {
                            showInternetConnectivityDialog();
                        }
                    }
                });
    }

    private void showInternetConnectivityDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.warning)
                .content(R.string.internet_connectivity_message)
                .positiveText(R.string.retry)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        checkInternetConnectivity();
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .cancelable(false)
                .show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        filterString(newText);

        return false;
    }

    public void setList(double lat , double log, final List<Restaurant> list) {

        if(list!=null) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            ListAdapter la = new ListAdapter(this, R.layout.adapter_list, list,
                    lat, log);
            listView.setAdapter(la);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Parcelable listParcelable = Parcels.wrap(list.get(position));
                    Intent i = new Intent(getApplicationContext(), RestaurantsDetailsActivity.class);
                    i.putExtra("RESTAURANT", listParcelable);
                    startActivity(i);
                }
            });
        }

    }

    public void filterString(String filterText) {
        filteredList = new ArrayList<Restaurant>();
        if(!Objects.equals(filterText, " ")) {
        for(Restaurant rest : jsonString) {
            if(rest.getName().toLowerCase().contains(filterText.toLowerCase())) {
                filteredList.add(rest);
            }
        }

        setList(lat,log,filteredList);

        }
    }
}
