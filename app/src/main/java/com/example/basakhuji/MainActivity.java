package com.example.basakhuji;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private SearchView searchView;
    private ArrayList<PropertyList> propertyList;

    DocumentSnapshot document;
    String propertyId;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        propertyList = new ArrayList<>();
        AllPropertyList();

        // Search view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // Initialize bottom navigation view
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                return true;
            } else if (item.getItemId() == R.id.add) {
                startActivity(new Intent(MainActivity.this, AddPropertyActivity.class));
                return true;
            } else if (item.getItemId() == R.id.map) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit the app?")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finishAffinity();  // Close all activities and exit the app
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); // Dismiss the dialog
                    }
                })
                .show();
    }

    private void filterList(String newText) {
        List<PropertyList> filteredList = new ArrayList<>();
        for (PropertyList property : propertyList) {
            if (property.getArea().toLowerCase().contains(newText.toLowerCase())
                    || property.getSubArea().toLowerCase().contains(newText.toLowerCase())
                    || property.getDistrict().toLowerCase().contains(newText.toLowerCase())) {

                filteredList.add(property);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No property found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    // Method to populate propertyList with sample data
    private void AllPropertyList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("properties")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(MainActivity.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        propertyList.clear(); // Clear the existing list to avoid duplication

                        for (QueryDocumentSnapshot document : value) {
                            String propertyId = document.getId();
                            PropertyList property = document.toObject(PropertyList.class);
                            property.setId(propertyId);
                            propertyList.add(property);
                        }

                        adapter = new PropertyAdapter(this, propertyList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
                    } else {
                        Toast.makeText(MainActivity.this, "No data available", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
