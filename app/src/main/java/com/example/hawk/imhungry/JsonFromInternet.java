package com.example.hawk.imhungry;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by hawk on 11/14/17.
 */

class JsonFromInternet extends AsyncTask<String, String, String> {
    private MyAsyncTaskListener mListener;
    public final String URL_LINK = "http://thormobileve.com/restaurants.json";
    final public void setListener(MyAsyncTaskListener listener) {
        mListener = listener;
    }
    protected void onPreExecute() {
        super.onPreExecute();
        if (mListener != null)
            mListener.onPreExecuteConcluded();
    }

    protected String doInBackground(String... params) {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(URL_LINK);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.onPostExecuteConcluded(constructUsingGson(result));
    }

    public interface MyAsyncTaskListener {
        void onPreExecuteConcluded();
        void onPostExecuteConcluded(List<Restaurant> result);
    }

    public List<Restaurant> constructUsingGson(String jsonString) {
        Gson gson = new GsonBuilder().create();
        Type listType =  new TypeToken<List<Restaurant>>() {}.getType();
        return gson.fromJson(jsonString, listType);
    }
}
