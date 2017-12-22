package com.example.hawk.imhungry.utilities;

import com.example.hawk.imhungry.models.Restaurant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by hawk on 11/28/17.
 */

public class AppHelpers {

    public static double meterDistanceBetweenPoints(double lat_a, double lng_a, float lat_b, float lng_b) {
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

    public static List<Restaurant> constructRestaurantsUsingGson(String jsonString) {
        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<List<Restaurant>>() {
        }.getType();
        return gson.fromJson(jsonString, listType);
    }
}
