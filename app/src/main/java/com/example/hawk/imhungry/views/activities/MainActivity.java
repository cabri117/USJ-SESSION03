package com.example.hawk.imhungry.views.activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.hawk.imhungry.utilities.AppHelpers;
import com.example.hawk.imhungry.R;
import com.example.hawk.imhungry.utilities.JsonFromInternet;
import com.example.hawk.imhungry.views.fragments.RestaurantDetailsFragment;
import com.example.hawk.imhungry.views.fragments.RestaurantListFragment;
import com.example.hawk.imhungry.utilities.URLContants;
import com.example.hawk.imhungry.models.Restaurant;
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

public class MainActivity extends AppCompatActivity implements JsonFromInternet.MyAsyncTaskListener,
        SearchView.OnQueryTextListener, RestaurantListFragment.OnRestaurantSelectedListener {
    MaterialProgressBar progressBar;
    JsonFromInternet jFI;
    SearchView searchView;
    SearchManager searchManager;
    private Bundle mBundleActivity;

    private List<Restaurant> jsonString;
    private List<Restaurant> filteredList;

    double lat;
    double log;

    boolean isFiltering;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBundleActivity = savedInstanceState;
        Toolbar toolbar = findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.pb);

        lat = 0.0;
        log = 0.0;
        jsonString = new ArrayList<>();
        filteredList = new ArrayList<>();
        isFiltering = false;

        jFI = new JsonFromInternet.Builder(URLContants.RESTAURANTS).build();
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
    protected void onDestroy() {
        super.onDestroy();
        if (!jFI.isCancelled()) {
            jFI.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (getCurrentFocus() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            0);
                }
                return true;
            }
        });

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
                if (isDownloadedFromInternet()) {
                    Intent i = new Intent(this, MapsActivity.class);
                    Parcelable listParcelable = Parcels.wrap(jsonString);
                    i.putExtra("RESTAURANT_LIST", listParcelable);
                    i.putExtra("actualLog", log);
                    i.putExtra("actualLat", lat);
                    startActivity(i);
                }
                break;
            case R.id.search:
                if (isDownloadedFromInternet()) {
                    searchView.setSearchableInfo(searchManager.
                            getSearchableInfo(getComponentName()));
                    searchView.setFocusable(true);
                    searchView.setIconified(false);
                    searchView.setSubmitButtonEnabled(false);
                    searchView.setOnQueryTextListener(this);

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkGPSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (mBundleActivity != null) {
                return;
            }

            // Create an instance of ExampleFragment
            RestaurantListFragment restaurantListFragment = RestaurantListFragment.createInstance(lat, log, jsonString);

            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            restaurantListFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, restaurantListFragment).commit();
        } else {
            RestaurantListFragment restaurantListFragment = (RestaurantListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.list_fragment);

            if (restaurantListFragment != null)
                restaurantListFragment.setupListAdapter(lat, log, jsonString);
        }

        progressBar.setVisibility(View.GONE);
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
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPostExecuteConcluded(String result) {
        if (result != null)
            jsonString = AppHelpers.constructRestaurantsUsingGson(result);
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
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterString(newText);
        return false;
    }

    public void filterString(String filterText) {
        filteredList = new ArrayList<>();
        if (!Objects.equals(filterText, " ")) {
            for (Restaurant rest : jsonString) {
                if (rest.getName().toLowerCase().contains(filterText.toLowerCase())) {
                    filteredList.add(rest);
                }
            }

            RestaurantListFragment restaurantListFrag;

            if (findViewById(R.id.fragment_container) != null) {
                restaurantListFrag = (RestaurantListFragment) getSupportFragmentManager().
                        findFragmentById(R.id.fragment_container);
            } else {
                restaurantListFrag = (RestaurantListFragment) getSupportFragmentManager().
                        findFragmentById(R.id.list_fragment);
            }

            if (restaurantListFrag == null)
                return;

            isFiltering = true;
            restaurantListFrag.setupListAdapter(lat, log, filteredList);
        } else isFiltering = false;
    }

    public boolean isDownloadedFromInternet() {
        if (jsonString != null) {
            return true;
        } else {
            Toast.makeText(this, getString(R.string.c_i_i), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onRestaurantSelected(int position) {

        Restaurant selectedRestaurant;
        if (isFiltering)
            selectedRestaurant = filteredList.get(position);
        else selectedRestaurant = jsonString.get(position);

        // Capture the restaurant fragment from the activity layout
        RestaurantDetailsFragment restaurantDetailsFragment = (RestaurantDetailsFragment)
                getSupportFragmentManager().findFragmentById(R.id.details_fragment);

        if (restaurantDetailsFragment != null) {
            // If restaurant frag is available, we're in two-pane layout...
            restaurantDetailsFragment.setupViews(selectedRestaurant);
        } else {
            // If the frag is not available, we're in the one-pane layout...
            Parcelable parcelable = Parcels.wrap(selectedRestaurant);
            Intent i = new Intent(getApplicationContext(), RestaurantDetailsActivity.class);
            i.putExtra("RESTAURANT", parcelable);
            i.putExtra("actualLog", log);
            i.putExtra("actualLat", lat);
            startActivity(i);
        }
    }
}
