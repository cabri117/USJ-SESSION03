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

    private double meterDistanceBetweenPoints(double lat_a, double lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

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
