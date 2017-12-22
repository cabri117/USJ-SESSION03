package com.example.hawk.imhungry.utilities;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hawk on 11/14/17.
 */

public class JsonFromInternet extends AsyncTask<String, String, String> {
    private MyAsyncTaskListener mListener;
    private String mUrl;

    private JsonFromInternet(Builder builder) {
        mUrl = builder.mUrl;
    }

//    public void setUrl(String url) {
//        if (TextUtils.isEmpty(url))
//            url = URLContants.RESTAURANTS;
//        else mUrl = url;
//    }

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
            URL url = new URL(mUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
            }

            return buffer.toString();
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
            mListener.onPostExecuteConcluded(result);
    }

    public interface MyAsyncTaskListener {
        void onPreExecuteConcluded();

        void onPostExecuteConcluded(String result);
    }

    public static class Builder {
        private final String mUrl;

        public Builder(String url) {
            if (TextUtils.isEmpty(url))
                url = URLContants.RESTAURANTS;

            this.mUrl = url;
        }

        public JsonFromInternet build() {
            return new JsonFromInternet(this);
        }
    }
}
