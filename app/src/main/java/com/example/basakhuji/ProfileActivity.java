package com.example.basakhuji;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity{
    private TextView usernameTextView;
    private EditText fullNameEditText, emailEditText, phoneNumberEditText, usernameEditText;
    private Button updateButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);


        ImageView backButton = findViewById(R.id.backButton);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();



        usernameTextView = findViewById(R.id.usernameTextView);
        usernameEditText = findViewById(R.id.usernameEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        updateButton = findViewById(R.id.updateButton);

        // Set email and username fields non-editable
        emailEditText.setEnabled(false);
        usernameEditText.setEnabled(false);

        // Set click listener for update button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDetails();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        } else {
            displayUserDetails(currentUser.getUid());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove Firestore listener
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void displayUserDetails(String userId) {
        // Query Firestore for user details
        userListener = db.collection("users").document(userId)
                .addSnapshotListener(this, (documentSnapshot, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Get user details from Firestore
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        String username = documentSnapshot.getString("username");

                        // Update EditText fields with user details
                        fullNameEditText.setText(fullName);
                        emailEditText.setText(email);
                        phoneNumberEditText.setText(phoneNumber);
                        Log.d(TAG, "phone number is: " + phoneNumber);
                        usernameEditText.setText(username);
                    }
                });
    }

    private void updateUserDetails() {
        // Get updated user details from EditText fields
        String fullName = fullNameEditText.getText().toString().trim();
        //String email = emailEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        // Validate fields
        if (fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create updated user details map
        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("fullName", fullName);
        //updatedUser.put("email", email);
        updatedUser.put("phoneNumber", phoneNumber);

        // Update user details in Firestore
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .update(updatedUser)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "User details updated successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error updating user details: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

}
