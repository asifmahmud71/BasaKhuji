package com.example.basakhuji;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import java.util.Map;

import javax.annotation.Nullable;

public class RatingActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private LinearLayout ratingsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        AppCompatButton ratingButton = findViewById(R.id.ratingButton);
        ratingsLayout = findViewById(R.id.commentsLayout);

        ratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
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

                        ratingsLayout.removeAllViews();

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
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
                    }
                });
    }

}
