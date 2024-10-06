package com.example.basakhuji;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.rx2.Rx2Apollo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SignupActivity extends AppCompatActivity {

    private EditText fullNameEditText, signUpEmailEditText, phonenumberEditText, signUpPasswordEditText, unameEditText;
    private Button signUpButton;
    private TextView loginTextView, countryCodeTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private AutoCompleteTextView countryDropdown;
    private ApolloClient apolloClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        apolloClient = ApolloClient.builder()
                .serverUrl("https://countries.trevorblades.com/graphql")
                .build();

        signUpEmailEditText = findViewById(R.id.signUpEmailEditText);
        signUpPasswordEditText = findViewById(R.id.signUpPasswordEditText);
        phonenumberEditText = findViewById(R.id.phonenumberEditText);
        unameEditText = findViewById(R.id.unameEditText);
        signUpButton = findViewById(R.id.signUpButton);
        fullNameEditText = findViewById(R.id.nameEditText);
        loginTextView = findViewById(R.id.loginTextView);

        countryDropdown = findViewById(R.id.countryAutoCompleteTextView);
        countryCodeTextView = findViewById(R.id.countryCodeTextView);

        // Set up email validation
        signUpEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidEmail(s.toString())) {
                    signUpEmailEditText.setError("Invalid email address");
                    signUpButton.setEnabled(false);
                } else {
                    signUpEmailEditText.setError(null);
                    signUpButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set up phone number validation
        phonenumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidPhoneNumber(s.toString())) {
                    phonenumberEditText.setError("Invalid phone number");
                    signUpButton.setEnabled(false);
                } else {
                    phonenumberEditText.setError(null);
                    signUpButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Check if username is available
        unameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkUsernameAvailability(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Sign up button click listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = fullNameEditText.getText().toString().trim();
                String username = unameEditText.getText().toString().trim();
                String email = signUpEmailEditText.getText().toString().trim();
                String password = signUpPasswordEditText.getText().toString().trim();
                String phoneNumber = phonenumberEditText.getText().toString().trim();

                if (!isValidPhoneNumber(phoneNumber)) {
                    phonenumberEditText.setError("Invalid phone number");
                    return;
                }

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
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    sendEmailVerification(user);
                                } else {
                                    Toast.makeText(SignupActivity.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Login button click listener
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

        // Fetch country data from GraphQL
        fetchCountries();
    }

    private void fetchCountries() {
        CountriesQuery countriesQuery = CountriesQuery.builder().build();
        Rx2Apollo.from(apolloClient.query(countriesQuery))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Response<CountriesQuery.Data>>() {
                    @Override
                    public void accept(Response<CountriesQuery.Data> response) throws Exception {
                        if (response.data() != null) {
                            List<String> countryNames = new ArrayList<>();
                            final List<String> countryCodes = new ArrayList<>(); // Store country codes
                            for (CountriesQuery.Country country : response.data().countries()) {
                                countryNames.add(country.name());
                                countryCodes.add("+" + country.phone());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(SignupActivity.this, android.R.layout.simple_dropdown_item_1line, countryNames);
                            countryDropdown.setAdapter(adapter);

                            // Handle item selection from dropdown
                            countryDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedCountryCode = countryCodes.get(position);
                                    countryCodeTextView.setText(selectedCountryCode);
                                }
                            });
                        }
                    }
                });
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Phone number validation method
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneNumberPattern = "^0[0-9]{10}$";
        return phoneNumber.matches(phoneNumberPattern);
    }

    // Username availability check method
    private void checkUsernameAvailability(final String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                unameEditText.setError("Username already exists");
                            } else {
                                unameEditText.setError(null);
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Send email verification
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                    storeUserDetails();
                } else {
                    Toast.makeText(SignupActivity.this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Store user details in Firestore
    private void storeUserDetails() {
        String fullName = fullNameEditText.getText().toString().trim();
        String username = unameEditText.getText().toString().trim();
        String email = signUpEmailEditText.getText().toString().trim();
        String phoneNumber = phonenumberEditText.getText().toString().trim();
        String selectedCountry = countryDropdown.getText().toString();

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("username", username);
        user.put("email", email);
        user.put("phoneNumber", phoneNumber);
        user.put("country", selectedCountry);

        userRef.set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(SignupActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Failed to register user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}