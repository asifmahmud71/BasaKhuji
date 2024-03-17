package com.example.basakhuji;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.basakhuji.Models.PropertyList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyDetailsActivity extends AppCompatActivity {

    private PropertyList propertyList;

    private FirebaseFirestore firestore;
    private final FirebaseUser currentUser;
    TextView likeTextView, dislikeTextView;
    ImageView likeButton, dislikeButton;
    private ListenerRegistration likeListener;
    private ListenerRegistration dislikeListener;


    public PropertyDetailsActivity() {
        this.firestore = FirebaseFirestore.getInstance();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        // Retrieve Intent and check for necessary extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("property")) {
            propertyList = (PropertyList) intent.getSerializableExtra("property");
            //Log.d("PropertyDetailsActivity", "Property: " + propertyList.toString());
        } else {
            //Log.e("PropertyDetailsActivity", "Intent or property is null");
            finish(); // Close activity if property is null
            return;
        }

        // Bind views
        TextView locationTextView = findViewById(R.id.locationTextView);
        TextView categoryTextView = findViewById(R.id.categoryTextView);
        TextView flatTypeTextView = findViewById(R.id.flatTypeTextView);
        TextView priceTextView = findViewById(R.id.priceTextView);
        TextView addedDateTextView = findViewById(R.id.addedDateTextView);
        TextView availableMonthTextView = findViewById(R.id.availableMonthTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        ImageView propertyImageView = findViewById(R.id.propertyImageView);
        TextView beds = findViewById(R.id.bedsTextView);
        TextView baths = findViewById(R.id.bathsTextView);
        TextView conTextView = findViewById(R.id.conTextView);
        likeButton = findViewById(R.id.like_btn);
        likeTextView = findViewById(R.id.like_text);
        dislikeButton = findViewById(R.id.dislike_btn);
        dislikeTextView = findViewById(R.id.dislike_text);
        TextView conlabel = findViewById(R.id.conLabel);

        // Populate views with property details
        locationTextView.setText("Location   :       "+propertyList.getLocation());
        categoryTextView.setText("Category   :      "+propertyList.getCategory());
        flatTypeTextView.setText("Type of Property   :       "+propertyList.getFlatType());
        beds.setText("Beds   :       "+propertyList.getBeds());
        baths.setText("Baths   :       "+propertyList.getBaths());
        priceTextView.setText("Price   :       "+propertyList.getPrice());
        addedDateTextView.setText("Added at   :       "+propertyList.getAddedDate());
        availableMonthTextView.setText("Availabe From   :       "+propertyList.getAvailableMonth());
        descriptionTextView.setText("Description   :       "+propertyList.getDescription());
        conTextView.setText("Phone Number   :    " +propertyList.getUserPhone());

        conlabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conTextView.setVisibility(View.VISIBLE);
            }
        });

        // Load property image using Glide
        Glide.with(this)
                .load(propertyList.getImageUrl())
                .into(propertyImageView);

        // Handle like button click
        likeButton.setOnClickListener(v -> {
            likeProperty(propertyList);
        });

        // Handle dislike button click
        dislikeButton.setOnClickListener(v -> {
            dislikeProperty(propertyList);
        });

        addLikeListener();
        addDislikeListener();
    }

    private void likeProperty(PropertyList property) {
        firestore.collection("properties").document(property.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> likedBy = (List<String>) document.get("likedBy");
                            List<String> dislikedBy = (List<String>) document.get("dislikedBy");
                            int currentLikeCount = likedBy != null ? likedBy.size() : 0;
                            if (likedBy != null && likedBy.contains(currentUser.getUid())) {
                                // User already liked the property, remove like
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("likedBy", FieldValue.arrayRemove(currentUser.getUid()));
                                updates.put("likeCount", currentLikeCount - 1);
                                firestore.collection("properties").document(property.getId())
                                        .update(updates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Update UI
                                                likeButton.setImageResource(R.drawable.like);
                                                likeTextView.setText(String.valueOf(currentLikeCount - 1));
                                                Toast.makeText(PropertyDetailsActivity.this, "Like removed", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(PropertyDetailsActivity.this, "Failed to remove like", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Like the property
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("likedBy", FieldValue.arrayUnion(currentUser.getUid()));
                                updates.put("likeCount", currentLikeCount + 1);
                                // Remove dislike if user has disliked the property
                                if (dislikedBy != null && dislikedBy.contains(currentUser.getUid())) {
                                    updates.put("dislikedBy", FieldValue.arrayRemove(currentUser.getUid()));
                                }
                                firestore.collection("properties").document(property.getId())
                                        .update(updates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Update UI
                                                likeButton.setImageResource(R.drawable.liked);
                                                likeTextView.setText(String.valueOf(currentLikeCount + 1));
                                                Toast.makeText(PropertyDetailsActivity.this, "Property liked", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(PropertyDetailsActivity.this, "Failed to like property", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void dislikeProperty(PropertyList property) {
        firestore.collection("properties").document(property.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> likedBy = (List<String>) document.get("likedBy");
                            List<String> dislikedBy = (List<String>) document.get("dislikedBy");
                            int currentDislikeCount = dislikedBy != null ? dislikedBy.size() : 0;
                            if (dislikedBy != null && dislikedBy.contains(currentUser.getUid())) {
                                // User already disliked the property, remove dislike
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("dislikedBy", FieldValue.arrayRemove(currentUser.getUid()));
                                updates.put("dislikeCount", currentDislikeCount - 1);
                                firestore.collection("properties").document(property.getId())
                                        .update(updates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Update UI
                                                dislikeButton.setImageResource(R.drawable.dislike);
                                                dislikeTextView.setText(String.valueOf(currentDislikeCount - 1));
                                                Toast.makeText(PropertyDetailsActivity.this, "Dislike removed", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(PropertyDetailsActivity.this, "Failed to remove dislike", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Dislike the property
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("dislikedBy", FieldValue.arrayUnion(currentUser.getUid()));
                                updates.put("dislikeCount", currentDislikeCount + 1);
                                // Remove like if user has liked the property
                                if (likedBy != null && likedBy.contains(currentUser.getUid())) {
                                    updates.put("likedBy", FieldValue.arrayRemove(currentUser.getUid()));
                                }
                                firestore.collection("properties").document(property.getId())
                                        .update(updates)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Update UI
                                                dislikeButton.setImageResource(R.drawable.disliked1);
                                                dislikeTextView.setText(String.valueOf(currentDislikeCount + 1));
                                                Toast.makeText(PropertyDetailsActivity.this, "Property disliked", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(PropertyDetailsActivity.this, "Failed to dislike property", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    // Method to add Firestore listener for changes in likedBy array
    private void addLikeListener() {
        likeListener = firestore.collection("properties").document(propertyList.getId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("PropertyDetailsActivity", "Listen failed", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        List<String> likedBy = (List<String>) value.get("likedBy");
                        if (likedBy != null) {
                            int likeCount = likedBy.size();
                            likeTextView.setText(String.valueOf(likeCount));
                            if (likedBy.contains(currentUser.getUid())) {
                                likeButton.setImageResource(R.drawable.liked);
                            } else {
                                likeButton.setImageResource(R.drawable.like);
                            }
                        } else {
                            likeTextView.setText("0");
                            likeButton.setImageResource(R.drawable.like);
                        }
                    }
                });
    }

    // Method to add Firestore listener for changes in dislikedBy array
    private void addDislikeListener() {
        dislikeListener = firestore.collection("properties").document(propertyList.getId())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("PropertyDetailsActivity", "Listen failed", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        List<String> dislikedBy = (List<String>) value.get("dislikedBy");
                        if (dislikedBy != null) {
                            int dislikeCount = dislikedBy.size();
                            dislikeTextView.setText(String.valueOf(dislikeCount));
                            if (dislikedBy.contains(currentUser.getUid())) {
                                dislikeButton.setImageResource(R.drawable.disliked1);
                            } else {
                                dislikeButton.setImageResource(R.drawable.dislike);
                            }
                        } else {
                            dislikeTextView.setText("0");
                            dislikeButton.setImageResource(R.drawable.dislike);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firestore listeners to avoid memory leaks
        if (likeListener != null) {
            likeListener.remove();
        }
        if (dislikeListener != null) {
            dislikeListener.remove();
        }
    }
}
