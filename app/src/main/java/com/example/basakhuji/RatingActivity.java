package com.example.basakhuji;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class RatingActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private LinearLayout ratingsLayout;
    private int currentPage = 0;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageView backButton = findViewById(R.id.backButton);
        AppCompatButton ratingButton = findViewById(R.id.ratingButton);
        ratingsLayout = findViewById(R.id.commentsLayout);
        ImageView nextButton = findViewById(R.id.nextButton);
        ImageView prevButton = findViewById(R.id.prevButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < totalPages - 1) {
                    currentPage++;
                    loadRatingsAndComments();
                } else {
                    Toast.makeText(RatingActivity.this, "Already on the last page", Toast.LENGTH_SHORT).show();
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage--;
                    loadRatingsAndComments();
                } else {
                    Toast.makeText(RatingActivity.this, "Already on the first page", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load existing ratings and comments
        loadRatingsAndComments();
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText commentEditText = dialogView.findViewById(R.id.commentEditText);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float rating = ratingBar.getRating();
                String comment = commentEditText.getText().toString().trim();

                saveRatingAndComment(rating, comment);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveRatingAndComment(float rating, String comment) {
        String userEmail = currentUser.getEmail();
        // Check if the user has already rated and commented
        firestore.collection("ratings")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // User hasn't rated and commented yet, proceed to save
                        CollectionReference ratingsRef = firestore.collection("ratings");

                        Map<String, Object> data = new HashMap<>();
                        data.put("userEmail", userEmail);
                        data.put("rating", rating);
                        data.put("comment", comment);

                        ratingsRef.add(data)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Rating and comment saved successfully", Toast.LENGTH_SHORT).show();
                                    // Refresh ratings and comments after adding a new one
                                    loadRatingsAndComments();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save rating and comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // User has already rated and commented
                        Toast.makeText(this, "You have already rated and commented", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error occurred while checking for existing ratings and comments
                    Toast.makeText(this, "Failed to check existing ratings and comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }


    private void loadRatingsAndComments() {
        firestore.collection("ratings")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(RatingActivity.this, "Failed to load ratings and comments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Calculate the total number of pages based on the number of ratings and comments
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        int totalItems = documents.size();
                        totalPages = (totalItems + 4) / 5; // Round up to the nearest whole number

                        // Clear the layout before loading new ratings and comments
                        ratingsLayout.removeAllViews();

                        // Display ratings and comments for the current page
                        int startIndex = currentPage * 5;
                        int endIndex = Math.min(startIndex + 5, totalItems);
                        for (int i = startIndex; i < endIndex; i++) {
                            DocumentSnapshot documentSnapshot = documents.get(i);
                            String userEmail = documentSnapshot.getString("userEmail");
                            float rating = Float.parseFloat(String.valueOf(documentSnapshot.getDouble("rating")));
                            String comment = documentSnapshot.getString("comment");

                            View ratingView = LayoutInflater.from(RatingActivity.this).inflate(R.layout.item_rating, null);

                            TextView userEmailTextView = ratingView.findViewById(R.id.userEmailTextView);
                            RatingBar ratingBar = ratingView.findViewById(R.id.ratingBar);
                            TextView commentTextView = ratingView.findViewById(R.id.commentTextView);

                            userEmailTextView.setText(userEmail);
                            ratingBar.setRating(rating);
                            commentTextView.setText(comment);

                            ratingsLayout.addView(ratingView);
                        }

                        // Update page numbers
                        updatePageNumbers();
                    }
                });
    }

    private void updatePageNumbers() {
        LinearLayout pageLayout = findViewById(R.id.pageLayout);
        pageLayout.removeAllViews();

        for (int i = 0; i < totalPages; i++) {
            TextView textView = new TextView(this);
            textView.setText(String.valueOf(i + 1));
            textView.setTag(i);
            textView.setPadding(16, 8, 16, 8);
            textView.setBackgroundResource(R.drawable.round_background);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
            textView.setGravity(Gravity.CENTER);
            textView.setHeight(100);
            textView.setWidth(100);

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPage = (int) v.getTag();
                    loadRatingsAndComments();
                }
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(8, 0, 8, 0);
            textView.setLayoutParams(layoutParams);

            if (i == currentPage) {
                // Set blue color for current page
                textView.setTextColor(Color.WHITE);
            }

            pageLayout.addView(textView);
        }
    }



}