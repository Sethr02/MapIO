package com.opsc.guideio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Settings extends AppCompatActivity {


    // Declaring necessary variables
    Login login = new Login();
    Button save, logout;
    TextView email;
    /*logout =

    findViewById(R.id.button2);*/

    // Firebase Variables (Firebase, 2022)
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(Login.uid).child("settings");
    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Users").child(Login.uid).child("email");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        email = findViewById(R.id.textView2);
        User use = new User();

        // Declaring variables for components
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter AA = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, use.Mesurements);
        spinner1.setAdapter(AA);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter AA2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, use.places);
        spinner2.setAdapter(AA2);
        // Declaring variables for components
        save = findViewById(R.id.button3);
        logout = findViewById(R.id.button2);

        ref2.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    email.setText(String.valueOf(task.getResult().getValue()));
                }
            }
        });

        save = findViewById(R.id.button3);

        // OnClick listener for save button
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creating HashMap (W3schools.com, 2022)
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("Unit", spinner1.getSelectedItem().toString());
                hashMap.put("Place", spinner2.getSelectedItem().toString());
                ref.setValue(hashMap);
                startActivity(new Intent(Settings.this, MapsActivity.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent
                startActivity(new Intent(Settings.this, Login.class));
            }
        });

    }
}