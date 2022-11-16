package com.opsc.guideio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewImagesActivity extends AppCompatActivity implements RecyclearViewInterface{

    RecyclerView placesRv;

    // Book Model ArrayList
    private ArrayList<PlacesModel> placesArrayList;

    // Instance of BookAdapter Class
    private PlacesAdapter placesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);

        placesRv = findViewById(R.id.placesRv);

        loadBookList();
    }

    private void loadBookList() {
        // init array before adding data
        placesArrayList = new ArrayList<>();

        // Book Database Reference (Firebase, 2022)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ImagesST10119434");
        // addValueEventListener (Firebase, 2022)
        ref.orderByChild("uid").equalTo(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear bookArrayList
                        placesArrayList.clear();
                        // DataSnapshot for loop (Firebase, 2022)
                        for (DataSnapshot ds: snapshot.getChildren()){
                            // Creating a BookModel with object from database
                            PlacesModel model = ds.getValue(PlacesModel.class);
                            // Adding model object to bookArrayList
                            placesArrayList.add(model);
                            // Log
                            //Log.d(TAG, "onDataChange: " + model.getId() + " " + model.getTitle());
                        }

                        // Setting Recycler View Layout Manager
                        placesRv.setLayoutManager(new LinearLayoutManager(ViewImagesActivity.this, LinearLayoutManager.VERTICAL, false));
                        // Setup Adapter
                        placesAdapter = new PlacesAdapter(ViewImagesActivity.this, placesArrayList);
                        // Set adapter to recyclerview
                        placesRv.setAdapter(placesAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onItemClick(int position) {
        Log.i("RecyclearView", "onItemClick: We clicked it");
    }
}