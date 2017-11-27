package com.example.hawk.imhungry;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/**
 * Created by hawk on 11/9/17.
 */

public class ListAdapter extends ArrayAdapter<Restaurant> {

    private List<Restaurant> mItems;
    private double mLatitud;
    private double mLongitud;

    public ListAdapter(Context context, int resource, List<Restaurant> items,
                       double latitud, double longitud) {
        super(context, resource, items);
        this.mItems = items;
        this.mLatitud = latitud;
        this.mLongitud = longitud;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.adapter_list, null);
        }

        ImageView imageView = v.findViewById(R.id.restProfilePic);
        GlideApp.with(v).load(mItems.get(position).getThumbnail()).circleCrop().into(imageView);

        TextView restName = v.findViewById(R.id.restName);
        restName.setText(mItems.get(position).getName());

        RatingBar mRatingBar = v.findViewById(R.id.ratingBar);
        mRatingBar.setRating((float) mItems.get(position).getRating());


        double distance = meterDistanceBetweenPoints(mLatitud, mLongitud, mItems.get(position).getLatitude(),
                mItems.get(position).getLongitude());

        TextView restDistance = v.findViewById(R.id.restDistance);
        restDistance.setText(String.format("%s KM",
                String.format(Locale.getDefault(), "%.2f", distance * 0.001)));

        return v;
    }

    private double meterDistanceBetweenPoints(double lat_a, double lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }
}
