package com.opsc.guideio;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class FetchURL extends AsyncTask<String, Void, String> {
    // Declaring Variables
    Context mContext;
    String directionMode = "driving";
    String urlData = "";
    String data = "";

    public FetchURL(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected String doInBackground(String... strings) {
        // For storing data from web service (W3schools.com, 2022)
        directionMode = strings[1];
        try {
            // Fetching the data from web service (W3schools.com, 2022)
            data = downloadUrl(strings[0]);
            urlData = data;
            Log.d("log", "Background task data " + data.toString());
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        PointsParser parserTask = new PointsParser(mContext, directionMode);
        // Invokes the thread for parsing the JSON data (GeeksforGeeks, 2021)
        parserTask.execute(s);

    }

    private String downloadUrl(String strUrl) throws IOException {
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url (GeeksforGeeks, 2021)
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url (GeeksforGeeks, 2021)
            urlConnection.connect();
            // Reading data from url (GeeksforGeeks, 2021)
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            urlData = data;
            Log.d("log", "Downloaded URL: " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("log", "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
