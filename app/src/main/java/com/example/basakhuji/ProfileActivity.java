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

    private String stringJavaScript = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "  <body>\n" +
            "    <!-- 1. The <iframe> (and video player) will replace this <div> tag. -->\n" +
            "    <div id=\"player\"></div>\n" +
            "\n" +
            "    <script>\n" +
            "      // 2. This code loads the IFrame Player API code asynchronously.\n" +
            "      var tag = document.createElement('script');\n" +
            "\n" +
            "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
            "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +


            "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
            "\n" +
            "      // 3. This function creates an <iframe> (and YouTube player)\n" +
            "      //    after the API code downloads.\n" +
            "      var player;\n" +
            "      function onYouTubeIframeAPIReady() {\n" +
            "        player = new YT.Player('player', {\n" +
            "          height: '195',\n" +
            "          width: '350',\n" +
            "          videoId: 'Vey-jmGJVNU',\n" +
            "          playerVars: {\n" +
            "            'playsinline': 1\n" +
            "          },\n" +
            "          events: {\n" +
            "            'onReady': onPlayerReady,\n" +
            "            'onStateChange': onPlayerStateChange\n" +
            "          }\n" +
            "        });\n" +
            "      }\n" +
            "\n" +
            "      // 4. The API will call this function when the video player is ready.\n" +
            "      function onPlayerReady(event) {\n" +
            "        event.target.playVideo();\n" +
            "      }\n" +
            "\n" +
            "      // 5. The API calls this function when the player's state changes.\n" +
            "      //    The function indicates that when playing a video (state=1),\n" +


            "      //    the player should play for six seconds and then stop.\n" +
            "      var done = false;\n" +
            "      function onPlayerStateChange(event) {\n" +
            "        if (event.data == YT.PlayerState.PLAYING && !done) {\n" +
            "          setTimeout(stopVideo, 1000000);\n" +
            "          done = true;\n" +
            "        }\n" +
            "      }\n" +
            "      function stopVideo() {\n" +
            "        player.stopVideo();\n" +
            "      }\n" +
            "    </script>\n" +
            "  </body>\n" +
            "</html>";


    private WebView webView;


    private TextView usernameTextView;
    private EditText fullNameEditText, emailEditText, phoneNumberEditText, usernameEditText;
    private Button updateButton, logoutButton, map_btn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;
    String email;

    private Retrofit retrofit;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView weatherConditionTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        ImageView backButton = findViewById(R.id.backButton);

        map_btn = findViewById(R.id.map_btn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
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
        logoutButton = findViewById(R.id.logoutButton);

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

        // Set click listener for logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logoutUser();
            }
        });

        // Initialize Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Initialize UI elements
        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        weatherConditionTextView = findViewById(R.id.weatherConditionTextView);

        // Fetch weather data when activity starts
        fetchWeatherData();

    }

    private void fetchWeatherData() {
        // Create API service interface using Retrofit
        WeatherApiService apiService = retrofit.create(WeatherApiService.class);

        // Make API call to fetch weather data
        Call<WeatherResponse> call = apiService.getWeather("Chittagong", "15a7403ff94164b39a76008df5594957");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();

                    // Extract weather information from response
                    double temperatureKelvin = weatherResponse.getMain().getTemp();
                    double temperatureCelsius = temperatureKelvin - 273.15;
                    double humidity = weatherResponse.getMain().getHumidity();
                    String weatherCondition = "";
                    if (weatherResponse != null && weatherResponse.getWeather() != null && !weatherResponse.getWeather().isEmpty()) {
                        weatherCondition = weatherResponse.getWeather().get(0).getDescription();
                    }

                    // Update UI with weather information
                    temperatureTextView.setText("Temperature: " + String.format("%.2f", temperatureCelsius) + "°C");
                    humidityTextView.setText("Humidity: " + humidity + "%");
                    weatherConditionTextView.setText("Weather: " + weatherCondition);
                } else {
                    // Handle unsuccessful API response
                    Toast.makeText(ProfileActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "error to fetch");
            }

        });
    }


    public void buttonPlayYouTubeVideo(View view) {
        webView.loadData(stringJavaScript, "text/html", "utf-8");
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

    private void logoutUser() {

        mAuth.signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finish();
    }

}
