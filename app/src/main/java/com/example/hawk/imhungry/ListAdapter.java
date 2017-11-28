package com.example.hawk.imhungry;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import static com.example.hawk.imhungry.utils.meterDistanceBetweenPoints;

/**
 * Created by hawk on 11/9/17.
 */

public class ListAdapter extends ArrayAdapter<Restaurant> {

    private List<Restaurant> items;
    RatingBar ratingBar;
    private double latitud;
    private double longitud;


    public ListAdapter(Context context, int resource, List<Restaurant> items,
                       double latitud, double longitud) {
        super(context, resource, items);
        this.items = items;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.adapter_list, null);
        }

        ImageView imageView = v.findViewById(R.id.restProfilePic);
        GlideApp.with(v).load(items.get(position).getThumbnail()).circleCrop().into(imageView);

        TextView restName = v.findViewById(R.id.restName);
        restName.setText(items.get(position).getName());

        ratingBar = v.findViewById(R.id.ratingBar);
        ratingBar.setRating((float) items.get(position).getRating());


        double distance = meterDistanceBetweenPoints(latitud,longitud,items.get(position).getLatitude(),
                items.get(position).getLongitude());

        TextView restDistance = v.findViewById(R.id.restDistance);
        restDistance.setText(String.format("%s KM",
                String.format("%.2f", distance * 0.001)));




        return v;
    }



}
