package com.example.basakhuji;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;


import java.util.HashMap;
import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEditText, signUpEmailEditText, phonenumberEditText, signUpPasswordEditText, unameEditText;
    private Button signUpButton;
    private TextView loginTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUpEmailEditText = findViewById(R.id.signUpEmailEditText);
        signUpPasswordEditText = findViewById(R.id.signUpPasswordEditText);
        phonenumberEditText = findViewById(R.id.phonenumberEditText);
        unameEditText = findViewById(R.id.unameEditText);
        signUpButton = findViewById(R.id.signUpButton);
        fullNameEditText = findViewById(R.id.nameEditText);
        loginTextView = findViewById(R.id.loginTextView);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = fullNameEditText.getText().toString().trim();
                String username = unameEditText.getText().toString().trim();
                String email = signUpEmailEditText.getText().toString().trim();
                String phoneNumber = phonenumberEditText.getText().toString().trim();
                String password = signUpPasswordEditText.getText().toString().trim();

                if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign up success, send verification email
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    sendEmailVerification(user);
                                } else {
                                    // If sign up fails, display a message to the user.
                                    Toast.makeText(SignupActivity.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the login activity
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Email verification sent successfully
                    Toast.makeText(SignupActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                    // Store user details in Firestore
                    storeUserDetails();
                } else {
                    // Failed to send verification email
                    Toast.makeText(SignupActivity.this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storeUserDetails() {
        String fullName = fullNameEditText.getText().toString().trim();
        String username = unameEditText.getText().toString().trim();
        String email = signUpEmailEditText.getText().toString().trim();
        String phoneNumber = phonenumberEditText.getText().toString().trim();

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("username", username);
        user.put("email", email);
        user.put("phoneNumber", phoneNumber);

        userRef.set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
