package com.example.hawk.imhungry;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.parceler.Parcels;

public class RestaurantDetailsFragment extends Fragment {

    private ConstraintLayout constraintLayout;
    private ImageView ivThumbnail;
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvPhone;
    private RatingBar rbRating;
    private TextView tvDescription;
    private FloatingActionButton fabCall, fabMap;

    private Restaurant mRestaurant;
    double lat = 0.0;
    double log = 0.0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_details, container, false);

        // Identifying Views
        constraintLayout = view.findViewById(R.id.constraintLayout);
        ivThumbnail = view.findViewById(R.id.ivThumbnail);
        tvName = view.findViewById(R.id.tvName);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvPhone = view.findViewById(R.id.tvPhone);
        rbRating = view.findViewById(R.id.rbRating);
        fabCall = view.findViewById(R.id.fabCall);
        fabMap = view.findViewById(R.id.fabMap);
        tvDescription = view.findViewById(R.id.tvDescription);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();



        Bundle args = getArguments();
        if (args == null) {
            constraintLayout.setVisibility(View.GONE);
            return;
        }
        lat = args.getDouble("actualLat", 0.0);
        log = args.getDouble("actualLog", 0.0);

        mRestaurant = Parcels.unwrap(args.getParcelable("RESTAURANT"));
        setupViews(mRestaurant);
    }

    protected void setupViews(Restaurant restaurant) {
        if (restaurant == null) {
            constraintLayout.setVisibility(View.GONE);
            return;
        }

        mRestaurant = restaurant;

        // Setting up Views
        getActivity().setTitle(getString(R.string.app_name) + " - " + mRestaurant.getName());

        tvName.setText(mRestaurant.getName());
        tvAddress.setText(mRestaurant.getAddress());
        tvPhone.setText(mRestaurant.getPhone());
        rbRating.setRating((float) mRestaurant.getRating());
        tvDescription.setText(mRestaurant.getDescription());

        GlideApp.with(this)
                .load(mRestaurant.thumbnail)
                .placeholder(R.mipmap.ic_launcher)
                .into(ivThumbnail);

        fabCall.setOnClickListener(onBtnCallClickListener);
        fabMap.setOnClickListener(onBtnFabMapClickListener);

        constraintLayout.setVisibility(View.VISIBLE);
    }
    /*@Override
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
                i.putExtra("actualLog", log);
                i.putExtra("actualLat", lat);
                i.putExtras(bundle);
                startActivity(i);
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

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

    private View.OnClickListener onBtnFabMapClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mRestaurant == null)
                return;

            Bundle bundle = new Bundle();
            bundle.putParcelable("RESTAURANT_LOCATION", Parcels.wrap(mRestaurant));
            Intent i = new Intent(getActivity(), MapsActivity.class);
            i.putExtras(bundle);
            startActivity(i);
        }
    };

    public boolean isTelephonyEnabled() {
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (tm != null) {
            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_GSM) {
                return this.getContext().getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
            } else {
                return tm.getSimState() == TelephonyManager.SIM_STATE_READY;
            }
        } else {
            return false;
        }
    }
}
