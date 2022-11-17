package com.opsc.guideio;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opsc.guideio.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    ImageButton menu, imageButton6;
    Button save;

    private static final String TAG = "";
    private static final float DEFAULT_ZOOM = 15;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient client;
    private Location currentLocation;
    private Place targetLocation;
    private Polyline currentPolyline;
    private InfoAdapter infoAdapter;
    private String distanceToDestination;
    private String estimatedTimeToDestination;
    private MarkerOptions markerOptions;

    String uid = FirebaseAuth.getInstance().getUid();

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);

    String x, y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();

        //initialize client (GeeksforGeeks, 2021)
        client = LocationServices.getFusedLocationProviderClient(this);

        menu.setOnClickListener(view -> startActivity(new Intent(MapsActivity.this, Settings.class)));

        // Permissions (GeeksforGeeks, 2021)
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            // Request permission (GeeksforGeeks, 2021)
            ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }

        binding.gpsRecenter.setOnClickListener(view -> getCurrentLocation());

        imageButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                x=String.valueOf(currentLocation.getLatitude());
                y=String.valueOf(currentLocation.getLongitude());
                // Craig This is For You
                // Intent takes you to PlacesActivity
                Intent placesIntent = new Intent(MapsActivity.this, PlacesActivity.class);
                // Passing x coordinate to PlacesActivity via Intent
                placesIntent.putExtra("x", x);
                // Passing y coordinate to PlacesActivity via Intent
                placesIntent.putExtra("y", y);
                // Starting Activity with values
                startActivity(placesIntent);
            }
        });
    }

    private void initViews() {
        menu = findViewById(R.id.imageButton3);
        imageButton6 = findViewById(R.id.imageButton6);
        save = findViewById(R.id.button);
        save.setVisibility(View.INVISIBLE);
    }

    private void moveCamera(LatLng latLng, float defaultZoom, String title) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(markerOptions);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                initializeMap();
            }
        });
    }

    private void initializeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used. (GeeksforGeeks, 2021)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        // initialize autocomplete (GeeksforGeeks, 2021)
        String apiKey = getString(R.string.apiKey);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        PlacesClient placesClient = Places.createClient(MapsActivity.this);

        // Initialize the AutocompleteSupportFragment. (GeeksforGeeks, 2021)
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //location bias (GeeksforGeeks, 2021)
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(currentLocation.getLongitude(), currentLocation.getLatitude()),
                new LatLng(currentLocation.getLongitude(), currentLocation.getLatitude())
        ));

        // Specify the types of place data to return. (GeeksforGeeks, 2020)
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response. (GeeksforGeeks, 2020)
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place. (GeeksforGeeks, 2020)
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                //init
                targetLocation = place;
                Log.d(TAG, "onPlaceSelected: " + targetLocation);

                //show place (GeeksforGeeks, 2020)
                moveCamera(targetLocation.getLatLng(), DEFAULT_ZOOM, place.getName());

                //get directions (GeeksforGeeks, 2020)
                getDirections(targetLocation);

                //init infoAdapter
                infoAdapter = null;
                infoAdapter = new InfoAdapter(
                        currentLocation,
                        MapsActivity.this,
                        distanceToDestination,
                        estimatedTimeToDestination,
                        targetLocation);
                try {

                    // Creating HashMap (W3schools.com, 2022)
                    HashMap<String, String> saved = new HashMap<String, String>();

                    mMap.setInfoWindowAdapter(infoAdapter);

                    Toast.makeText(MapsActivity.this, targetLocation.getAddress(), Toast.LENGTH_SHORT).show();

                    saved.put("Address", targetLocation.getAddress());
                    saved.put("Name", targetLocation.getName());
                    saved.put("Longitude", String.valueOf(targetLocation.getLatLng().longitude));
                    saved.put("Latitude", String.valueOf(targetLocation.getLatLng().latitude));
                    saved.put("Phone Number", targetLocation.getPhoneNumber());
                    ref.get().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        } else {
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        }
                    });


                    ref.child("Landmarks").setValue(saved);


                } catch (Exception e) {
                    Log.i(TAG, "onSuccess: " + e);
                }

                //reset distanceToDestination & estimatedTimeToDestination// Creating HashMap (W3schools.com, 2022)
                distanceToDestination = "N/A";
                estimatedTimeToDestination = "N/A";
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void getDirections(Place targetLocation) {
        String s = Uri.parse("https://maps.googleapis.com/maps/api/directions/json?")
                .buildUpon()
                .appendQueryParameter("origin", currentLocation.getLatitude() + "," + currentLocation.getLongitude())
                .appendQueryParameter("destination", targetLocation.getLatLng().latitude + "," + targetLocation.getLatLng().longitude)
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", getString(R.string.apiKey))
                .toString();

        Log.i(TAG, "getDirections: " + s);

        try {
            s = new FetchURL(MapsActivity.this).execute(s, "driving").get();

            JSONObject jsonObject = new JSONObject();
            jsonObject = new JSONObject(s);

            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONArray legs = ((JSONObject) routes.get(0)).getJSONArray("legs");

            //GET DISTANCE AND ESTIMATED TIME DIRECTIONS (W3schools.com, 2022)
            String distance = legs.getJSONObject(0).getJSONObject("distance").getString("text");
            String estTime = legs.getJSONObject(0).getJSONObject("duration").getString("text");

            //set values (W3schools.com, 2022)
            distanceToDestination = distance;
            estimatedTimeToDestination = estTime;

            Log.d(TAG, "getDirections: Distance " + distance);
            Log.d(TAG, "getDirections: EstimatedTime " + estTime);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String cl = "Coordinates: " + currentLocation.getLatitude() + " : " + currentLocation.getLongitude();

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        Toast.makeText(MapsActivity.this, cl, Toast.LENGTH_SHORT).show();

        // Setting map properties (W3schools.com, 2022)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String xCoord = extras.getString("x");
            String yCoord = extras.getString("y");
            //kenju (1958) How do I get extra data from intent on Android?, Stack Overflow. Available at: https://stackoverflow.com/questions/4233873/how-do-i-get-extra-data-from-intent-on-android (Accessed: November 17, 2022).
            placeMarker(xCoord, yCoord);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void placeMarker(String xCoord, String yCoord) {
        try {
            LatLng coord = new LatLng(Double.parseDouble(xCoord), Double.parseDouble(yCoord));
            mMap.addMarker(new MarkerOptions().position(coord).title(
                "POI"
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Adding a Map with a Marker (no date) Google. Google. Available at: https://developers.google.com/maps/documentation/android-sdk/map-with-marker (Accessed: November 17, 2022).

    }
}