package com.opsc.guideio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class Login extends AppCompatActivity {


    TextInputEditText email, password;
    TextInputLayout emailTil, passwordTil;
    Button signInBtn;
    ImageButton backBTN;
    TextView noAcc;
    CircularProgressIndicator progressBar;
    static String  uid;

    // Firebase auth (Firebase, 2022)
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // init views
        initViews();

        // init Firebase auth (Firebase, 2022)
        firebaseAuth = FirebaseAuth.getInstance();

        // onClick method for back button (GeeksforGeeks, 2019)
        backBTN.setOnClickListener(view -> startActivity(new Intent(Login.this, MapsActivity.class)));

        // onClick method for sign in button
        signInBtn.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            validateData();
        });

        // onClick method for don't have account intent (GeeksforGeeks, 2019)
        noAcc.setOnClickListener(view -> {
            // Intent for when user doesn't have an account it takes user to signup (GeeksforGeeks, 2019)
            startActivity(new Intent(Login.this, Register.class));
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordTil.setError(null);//removes error
                emailTil.setError(null);//removes error
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private String emailVal = "", passwordVal = "";

    private void validateData() {
        emailVal = email.getText().toString().trim();
        passwordVal = password.getText().toString().trim();

        // Validate Data
        if (!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()){
            emailTil.setErrorEnabled(true);
            emailTil.setError("Invalid Email!");
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if (TextUtils.isEmpty(passwordVal)){
            passwordTil.setErrorEnabled(true);
            passwordTil.setError("Enter Password!");
            progressBar.setVisibility(View.INVISIBLE);
        }
        else {
            // Data is Validated before Login
            loginUser();
        }
    }

    private void loginUser() {
        // Login User
        firebaseAuth.signInWithEmailAndPassword(emailVal, passwordVal)
                .addOnSuccessListener(authResult -> {
                    // Successfully Logged in
                    startActivity(new Intent(Login.this, MapsActivity.class));
                    checkUser();
                })
                .addOnFailureListener(e -> {
                    // Failed to Log in
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(Login.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUser() {
        // Get Current User (Firebase, 2022)
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        uid=firebaseAuth.getUid();

        // Check for user in Real time Database (Firebase, 2022)
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    // (Firebase, 2022)
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get User Type (Firebase, 2022)
                        String userType = "" + snapshot.child("userType").getValue();

                        if (userType.equals("user")){
                            // When login is a success send user to dashboard activity (GeeksforGeeks, 2019)
                            startActivity(new Intent(Login.this, MapsActivity.class));

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void initViews() {
        email = findViewById(R.id.email_input);
        password = findViewById(R.id.password_input);
        signInBtn = findViewById(R.id.btnSignin);
        noAcc = findViewById(R.id.dontHaveAccount);
        progressBar = findViewById(R.id.progressBar);
        backBTN = findViewById(R.id.backBTN);
        emailTil = findViewById(R.id.emailTil);
        passwordTil = findViewById(R.id.passwordTil);
    }
}