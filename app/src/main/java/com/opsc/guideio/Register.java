package com.opsc.guideio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText surname,name, email, password, confirmPassword;
    Button registerBtn;
    ImageButton backBTN;
    TextView alreadyHaveAcc;
    CircularProgressIndicator progressBar;

    // Firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // calling initViews method to initialise views
        initViews();

        // Initialise Firebase Auth (Firebase, 2022)
        firebaseAuth = FirebaseAuth.getInstance();

        // Back button OnClick listener (GeeksforGeeks, 2019)
        backBTN.setOnClickListener(view -> startActivity(new Intent(Register.this, MapsActivity.class)));

        // Register button OnClick listener
        registerBtn.setOnClickListener(view -> {
            // Make progress visible
            progressBar.setVisibility(View.VISIBLE);
            // Call validateData method
            validateData();
        });

        // alreadyHaveAcc OnClick listener
        alreadyHaveAcc.setOnClickListener(view -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });
    }

    private String surnameVal= "", nameVal = "", emailVal = "", passwordVal = "";

    private void validateData() {
        // Get values from user input
        nameVal = name.getText().toString().trim();
        surnameVal = surname.getText().toString().trim();
        emailVal = email.getText().toString().trim();
        passwordVal = password.getText().toString().trim();
        String cPassword = confirmPassword.getText().toString().trim();

        // Validate Data
        if (TextUtils.isEmpty(nameVal)){
            // Display Toast if user hasn't entered a username
            Toast.makeText(this, "Enter a Username...", Toast.LENGTH_SHORT).show();
            // Hide progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()){
            // Display Toast if user hasn't entered a email
            Toast.makeText(this, "Invalid Email...", Toast.LENGTH_SHORT).show();
            // Hide progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if (TextUtils.isEmpty(passwordVal)){
            // Display Toast if user hasn't entered a Password
            Toast.makeText(this, "Enter Password...", Toast.LENGTH_SHORT).show();
            // Hide progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if (TextUtils.isEmpty(cPassword)){
            // Display Toast if user hasn't their Password again
            Toast.makeText(this, "Confirm Password...", Toast.LENGTH_SHORT).show();
            // Hide progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if (!passwordVal.equals(cPassword)){
            // Display Toast if user Passwords don't match
            Toast.makeText(this, "Passwords Don't Match...", Toast.LENGTH_SHORT).show();
            // Hide progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            // Calling createUserAccount method
            createUserAccount();
        }
    }

    private void createUserAccount() {
        // Create User in Firebase Auth (Firebase, 2022)
        firebaseAuth.createUserWithEmailAndPassword(emailVal, passwordVal)
                .addOnSuccessListener(authResult -> {
                    updateUserInfo();
                    // Hide progress bar
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(Register.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Hide progress bar
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(Register.this, "Failed to Create Account!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserInfo() {
        // Timestamp

        String timestamp = Long.toString(System.currentTimeMillis());

        // Get Current User uid (Firebase, 2022)
        String uid = firebaseAuth.getUid();

        // setup to add data in Realtime Db (Firebase, 2022)
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("email", emailVal);
        hashMap.put("name", nameVal);
        hashMap.put("surname", surnameVal);
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        // Add data to DB (Firebase, 2022)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    // Data successfully added
                    // Hide progress bar
                    progressBar.setVisibility(View.INVISIBLE);
                    // Displays toast when account is created successfully
                    Toast.makeText(Register.this, "Account Successfully Created.", Toast.LENGTH_SHORT).show();
                    // Intent to DashboardUserActivity (GeeksforGeeks, 2019)
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Data failed to add
                    // Hide progress bar
                    progressBar.setVisibility(View.INVISIBLE);
                    // Displays Toast if add failed
                    Toast.makeText(Register.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void initViews() {
        // Initialise Views
        name = findViewById(R.id.name_input);
        surname = findViewById(R.id.surname_input);
        email = findViewById(R.id.Email_input);
        password = findViewById(R.id.Password_input);
        confirmPassword = findViewById(R.id.confirm_password_input);
        registerBtn = findViewById(R.id.btnRegister);
        alreadyHaveAcc = findViewById(R.id.alreadyHaveAccount);
        backBTN = findViewById(R.id.backBTN);
        progressBar = findViewById(R.id.progressBar);
    }

}