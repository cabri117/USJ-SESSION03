package com.example.hawk.imhungry;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import org.parceler.Parcels;

public class RestaurantsDetailsActivity extends AppCompatActivity {

    private ImageView ivThumbnail;
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvPhone;
    private RatingBar rbRating;
    private FloatingActionButton fabCall;

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
        fabCall = findViewById(R.id.fabCall);

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

        fabCall.setOnClickListener(onBtnCallClickListener);
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

    private View.OnClickListener onBtnCallClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isTelephonyEnabled()) {
                //String uri = "tel:" + "(787) 262-4901";
                Intent intent = new Intent(Intent.ACTION_DIAL);
                //intent.setData(Uri.parse(uri));
                intent.setData(Uri.parse("tel:" + mRestaurant.getPhone()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                new MaterialDialog.Builder(v.getContext())
                        .title(R.string.warning)
                        .content(R.string.phone_error)
                        .positiveText(R.string.accept)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .autoDismiss(false)
                        .cancelable(false)
                        .show();
            }
        }
    };

    public boolean isTelephonyEnabled() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (tm != null) {
            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM) {
                return this.getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            } else {
                return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
            }
        } else {
            return false;
        }
    }
}
