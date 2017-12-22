package com.example.hawk.imhungry;

/**
 * Created by mcvasquez on 12/22/17.
 */

public class URLContants {
    public static final String RESTAURANTS = "http://thormobileve.com/restaurants.json";

    public static String makeRoute(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        return "http://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" +// from
                Double.toString(sourcelat) +
                "," +
                Double.toString(sourcelog) +
                "&destination=" +// to
                Double.toString(destlat) +
                "," +
                Double.toString(destlog) +
                "&sensor=false&mode=walking&alternatives=true";
    }
}
