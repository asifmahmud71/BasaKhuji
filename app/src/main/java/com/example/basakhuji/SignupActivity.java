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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private EditText fullNameEditText, signUpEmailEditText, phonenumberEditText, signUpPasswordEditText, unameEditText;
    private Button signUpButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    String userID;

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

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpUser();
            }
        });

        TextView loginTextView = findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate back to the login activity
                finish();
            }
        });
    }

    private void signUpUser() {
        String username, email, password, pnumber, fullname;
        username = unameEditText.getText().toString().trim();
        email = signUpEmailEditText.getText().toString().trim();
        password = signUpPasswordEditText.getText().toString().trim();
        pnumber = phonenumberEditText.getText().toString().trim();
        fullname = fullNameEditText.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || pnumber.isEmpty() || fullname.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, add user details to Firestore
                            Toast.makeText(SignupActivity.this, "Sign up success. ", Toast.LENGTH_SHORT).show();
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = db.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fullName", fullname);
                            user.put("username", username);
                            user.put("email", email);
                            user.put("phone", pnumber);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: user details saved" + userID);
                                }
                            });
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignupActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


//    private void saveUserToFirestore(String fullname, String email, String pnumber, String username) {
//        User user = new User(fullname, email, pnumber, username);
//
//        db.collection("users").document(username)
//                .set(user)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            // User details saved successfully
//                            Toast.makeText(SignupActivity.this, "Sign up successful.", Toast.LENGTH_SHORT).show();
//                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
//                            finish();
//                        } else {
//                            // Error saving user details to Firestore
//                            Toast.makeText(SignupActivity.this, "Error saving user details: " + task.getException(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
}
