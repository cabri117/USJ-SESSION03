package com.example.hawk.imhungry;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.parceler.Parcels;

public class RestaurantsDetailsActivity extends AppCompatActivity {

    private ImageView ivThumbnail;
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvPhone;
    private RatingBar rbRating;

    private Restaurant mRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants_details);

        // Identifying Views
        ivThumbnail = findViewById(R.id.ivThumbnail);
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        rbRating = findViewById(R.id.rbRating);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mRestaurant = Parcels.unwrap(getIntent().getParcelableExtra("RESTAURANT"));

        if (mRestaurant == null)
            return;

        // Setting up Views
        setTitle(mRestaurant.getName());

        tvName.setText(mRestaurant.getName());
        tvAddress.setText(mRestaurant.getAddress());
        tvPhone.setText(mRestaurant.getPhone());
        rbRating.setRating((float) mRestaurant.getRating());

        GlideApp.with(this)
                .load(mRestaurant.thumbnail)
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(ivThumbnail);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_restaurants_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.location:
                if (mRestaurant == null)
                    break;

                Bundle bundle = new Bundle();
                bundle.putParcelable("RESTAURANT_LOCATION", Parcels.wrap(mRestaurant));
                Intent i = new Intent(this, MapsActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
