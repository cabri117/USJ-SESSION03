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

import static com.example.hawk.imhungry.AppHelpers.meterDistanceBetweenPoints;

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



}
