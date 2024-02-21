package com.example.basakhuji;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.basakhuji.Adapter.PropertyAdapter;
import com.example.basakhuji.Models.PropertyList;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private List<PropertyList> propertyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        propertyList = new ArrayList<>();
        adapter = new PropertyAdapter(this, propertyList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        populatePropertyList();

        // Initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.favourite) {
                Toast.makeText(MainActivity.this, "The Favourite Properties are listed here", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.add) {
                startActivity(new Intent(MainActivity.this, AddPropertyActivity.class));
                return true;
            } else if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // Method to populate propertyList with sample data (replace this with Firestore data retrieval)
    private void populatePropertyList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("properties")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId(); // Get Firestore document ID
                            String imageUrl = document.getString("imageUrl");
                            String category = document.getString("category");
                            String flatType = document.getString("flatType");
                            String location = document.getString("location");
                            String price = document.getString("price");
                            String addedDate = document.getString("addedDate");
                            String availableMonth = document.getString("availableMonth");
                            String description = document.getString("description");
                            //long beds = document.getLong("beds");
                            //long baths = document.getLong("baths");

                            // Convert to integers if necessary
                            //int bedsInt = (int) beds;
                            //int bathsInt = (int) baths;


                            loadPropertyImage(imageUrl, category, location, flatType, price, addedDate, availableMonth, description, documentId);
                            Log.d("PropertyList", "imageUrl: " + imageUrl + ", category: " + category + ", flatType: " + flatType + ", location: " + location + ", addedDate: " + addedDate + ", availableMonth: " + availableMonth);
                        }
                    }else {
                        // Handle errors
                        Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadPropertyImage(String imageUrl, String category, String location, String flatType, String price, String addedDate, String availableMonth, String description, String documentId) {
        // Load image from Cloud Storage using Glide
        Glide.with(this)
                .load(imageUrl) // Cloud Storage URL
                .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                .error(R.drawable.error_image) // Error image if loading fails
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Create a new PropertyList object with the loaded image
                        PropertyList property = new PropertyList(imageUrl, category, flatType, location, price, addedDate, availableMonth, description, documentId);
                        propertyList.add(property);
                        adapter.notifyDataSetChanged(); // Notify adapter that data has changed
                    }
                });
    }

}