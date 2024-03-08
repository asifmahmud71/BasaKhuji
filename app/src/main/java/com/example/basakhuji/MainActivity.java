package com.example.basakhuji;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basakhuji.Adapter.PropertyAdapter;
import com.example.basakhuji.Models.PropertyList;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private ArrayList<PropertyList> propertyList;

    DocumentSnapshot document;
    String propertyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        propertyList = new ArrayList<>();
        AllPropertyList();


        // Initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                return true;
            } else if (item.getItemId() == R.id.favourite) {
                Toast.makeText(MainActivity.this, "The Favourite Properties are listed here", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.add) {
                startActivity(new Intent(MainActivity.this, AddPropertyActivity.class));
                return true;
            } else if (item.getItemId() == R.id.rating) {
                startActivity(new Intent(MainActivity.this, RatingActivity.class));
                return true;
            } else if (item.getItemId() == R.id.profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }


    // Method to populate propertyList with sample data (replace this with Firestore data retrieval)
    private void AllPropertyList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("properties")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String propertyId = document.getId();
                            PropertyList property = document.toObject(PropertyList.class);
                            property.setId(propertyId);
                            propertyList.add(property);
                        }

                        adapter = new PropertyAdapter(this, propertyList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

                    } else {
                        // Handle errors
                        Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}