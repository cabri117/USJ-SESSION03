package com.example.hawk.imhungry.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.hawk.imhungry.R;
import com.example.hawk.imhungry.views.fragments.RestaurantDetailsFragment;
import com.example.hawk.imhungry.models.Restaurant;

import org.parceler.Parcels;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private Restaurant mRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        // Check whether the activity is using the layout version with
        // the fragment_container FrameLayout. If so, we must add the first fragment
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            mRestaurant = Parcels.unwrap(getIntent().getParcelableExtra("RESTAURANT"));

            if (mRestaurant == null)
                return;

            RestaurantDetailsFragment restaurantDetailsFrag = new RestaurantDetailsFragment();
            Bundle args = new Bundle();
            args.putParcelable("RESTAURANT", getIntent().getParcelableExtra("RESTAURANT"));
            args.putDouble("actualLat", getIntent().getDoubleExtra("actualLat", 0.0));
            args.putDouble("actualLog", getIntent().getDoubleExtra("actualLog", 0.0));
            restaurantDetailsFrag.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, restaurantDetailsFrag)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
