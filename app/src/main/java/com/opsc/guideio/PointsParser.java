package com.opsc.guideio;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    TaskLoadedCallback taskCallback;
    String directionMode = "driving";

    public PointsParser(Context mContext, String directionMode) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        // Declaring JSONObject (Tutorialspoint.com, 2022)
        JSONObject jObject;
        // Declaring HashMap for routes (W3schools.com, 2022)
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            Log.d("log", jsonData[0].toString());
            DataParser parser = new DataParser();
            Log.d("log", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            Log.d("log", "Executing routes");
            Log.d("log", routes.toString());

        } catch (Exception e) {
            Log.d("log", e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process (GeeksforGeeks, 2021)
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        // Traversing through all the routes (GeeksforGeeks, 2021)
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route (GeeksforGeeks, 2021)
            List<HashMap<String, String>> path = result.get(i);
            // Fetching all the points in i-th route (GeeksforGeeks, 2021)
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // Adding all the points in the route to LineOptions (GeeksforGeeks, 2021)
            lineOptions.addAll(points);
            if (directionMode.equalsIgnoreCase("walking")) {
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.BLUE);
            }
            Log.d("log", "onPostExecute lineOptions decoded");
        }

        // Drawing polyline in the Google Map for the i-th route (GeeksforGeeks, 2021)
        if (lineOptions != null) {
            taskCallback.onTaskDone(lineOptions);

        } else {
            Log.d("log", "without PolyLines drawn");
        }
    }
}
