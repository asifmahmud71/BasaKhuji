// PropertyDetailsActivity.java
package com.example.basakhuji;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class PropertyDetailsActivity extends AppCompatActivity {

    private int likes = 0;
    private int dislikes = 0;
    private SharedPreferences sharedPreferences;
    private String propertyId;
    private FirebaseFirestore firestore;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        firestore = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieve saved counts of likes and dislikes for this property
        Intent intent = getIntent();
        // Update like and dislike counts
        updateLikeDislikeCount();

        ratingBar = findViewById(R.id.ratingBar);
        Button submitRatingButton = findViewById(R.id.submitRatingButton);

        // Retrieve property details from intent extras
        if (intent != null) {
            propertyId = intent.getStringExtra("id"); // Get property ID
            if (propertyId == null) {
                Log.e("PropertyDetailsActivity", "Property ID is null");
            } else {
                Log.d("PropertyDetailsActivity", "Property ID: " + propertyId);
            }

            likes = getLikesFromStorage(propertyId);
            dislikes = getDislikesFromStorage(propertyId);
            String location = intent.getStringExtra("location");
            String category = intent.getStringExtra("category");
            String flatType = intent.getStringExtra("flatType");
            String price = intent.getStringExtra("price");
            String imageUrl = intent.getStringExtra("imageUrl");
            String addedDate = intent.getStringExtra("addedDate");
            String availableMonth = intent.getStringExtra("availableMonth");
            String description = intent.getStringExtra("description");

            // Display property details
            TextView locationTextView = findViewById(R.id.locationTextView);
            locationTextView.setText(location);

            TextView categoryTextView = findViewById(R.id.categoryTextView);
            categoryTextView.setText(category);

            TextView flatTypeTextView = findViewById(R.id.flatTypeTextView);
            flatTypeTextView.setText(flatType);

            TextView priceTextView = findViewById(R.id.priceTextView);
            priceTextView.setText(price);

            TextView addedDateTextView = findViewById(R.id.addedDateTextView);
            addedDateTextView.setText(addedDate);

            TextView availableMonthTextView = findViewById(R.id.availableMonthTextView);
            availableMonthTextView.setText(availableMonth);

            TextView descriptionTextView = findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);

            ImageView propertyImageView = findViewById(R.id.propertyImageView);
            Glide.with(this).load(imageUrl).into(propertyImageView);

            // Initialize like and dislike buttons
            Button likeButton = findViewById(R.id.likeButton);
            Button dislikeButton = findViewById(R.id.dislikeButton);

            // Set onClickListeners for like and dislike buttons
            likeButton.setOnClickListener(v -> {
                incrementLikes(propertyId);
            });

            dislikeButton.setOnClickListener(v -> {
                incrementDislikes(propertyId);
            });
        }
        submitRatingButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            submitRating(rating);
        });

    }
    // Method to submit rating to Firestore
    private void submitRating(float rating) {
        if (propertyId != null) {
            firestore.collection("properties").document(propertyId)
                    .update("rating", rating)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PropertyDetailsActivity.this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(PropertyDetailsActivity.this, "Failed to submit rating", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(PropertyDetailsActivity.this, "Property ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update like and dislike counts on UI
    private void updateLikeDislikeCount() {
        TextView likeCountTextView = findViewById(R.id.likeCountTextView);
        TextView dislikeCountTextView = findViewById(R.id.dislikeCountTextView);

        likeCountTextView.setText(String.valueOf(likes));
        dislikeCountTextView.setText(String.valueOf(dislikes));
    }

    // Method to retrieve likes count from SharedPreferences
    private int getLikesFromStorage(String propertyId) {
        return sharedPreferences.getInt(propertyId + "_likes", 0);
    }

    // Method to save likes count to SharedPreferences
    private void saveLikesToStorage(String propertyId, int likes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(propertyId + "_likes", likes);
        editor.apply();
    }

    // Method to retrieve dislikes count from SharedPreferences
    private int getDislikesFromStorage(String propertyId) {
        return sharedPreferences.getInt(propertyId + "_dislikes", 0);
    }

    // Method to save dislikes count to SharedPreferences
    private void saveDislikesToStorage(String propertyId, int dislikes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(propertyId + "_dislikes", dislikes);
        editor.apply();
    }

    // Method to increment likes count and update UI
    private void incrementLikes(String propertyId) {
        likes++;
        saveLikesToStorage(propertyId, likes);
        updateLikeDislikeCount();
        Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show();
    }

    // Method to increment dislikes count and update UI
    private void incrementDislikes(String propertyId) {
        dislikes++;
        saveDislikesToStorage(propertyId, dislikes);
        updateLikeDislikeCount();
        Toast.makeText(this, "Disliked!", Toast.LENGTH_SHORT).show();
    }
}
