package com.opsc.guideio;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.model.Place;
import com.opsc.guideio.databinding.InfoWindowBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class InfoAdapter implements GoogleMap.InfoWindowAdapter {
    private InfoWindowBinding binding;
    private String distanceToDestination;
    private String estimatedTimeToDestination;
    private Location location;
    private Context context;
    private Place targetLocation;
    private List<Address> addresses;
    private Geocoder geocoder;

    public InfoAdapter(
            Location location,
            Context context,
            String distanceToDestination,
            String estimatedTimeToDestination,
            Place targetLocation) {
        this.location = location;
        this.context = context;
        this.distanceToDestination = distanceToDestination;
        this.estimatedTimeToDestination = estimatedTimeToDestination;
        this.targetLocation = targetLocation;

        //Geocoder and addresses initialization
        try {
            geocoder = new Geocoder(context, Locale.getDefault());
            addresses = geocoder.getFromLocation(
                    targetLocation.getLatLng().latitude,
                    targetLocation.getLatLng().longitude,
                    1);

            Log.d("log", "InfoAdapter: Address Information "+addresses);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("log", "InfoAdapter: distanceToDestination" + distanceToDestination);
        Log.d("log", "InfoAdapter: estimatedTimeToDestination" + estimatedTimeToDestination);

        binding = InfoWindowBinding.inflate(LayoutInflater.from(context), null, false);
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        binding.locationNameText.setText(marker.getTitle());
        binding.locationDistanceText.setText(distanceToDestination);
        binding.locationEstTimeText.setText(estimatedTimeToDestination);

        if(addresses != null && addresses.size() > 0){
            // Getting address info
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String province = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();

            binding.addressText.setText(address);
            binding.cityText.setText(city);
            binding.provinceText.setText(province);
            binding.countryText.setText(country);
            binding.postalCodeText.setText(postalCode);
        }
        //CONVERSION TO MILES

        return binding.getRoot();
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        binding.locationNameText.setText(marker.getTitle());;
        binding.locationDistanceText.setText(distanceToDestination);
        binding.locationEstTimeText.setText(estimatedTimeToDestination);

        if(addresses != null && addresses.size() > 0){
            // Getting address info (W3schools.com, 2021)
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String province = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();

            binding.addressText.setText(address);
            binding.cityText.setText(city);
            binding.provinceText.setText(province);
            binding.countryText.setText(country);
            binding.postalCodeText.setText(postalCode);
        }

        //CONVERSION TO MILES (W3schools.com, 2022)
        return binding.getRoot();

    }
}
