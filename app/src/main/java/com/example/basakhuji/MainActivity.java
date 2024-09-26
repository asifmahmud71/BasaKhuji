package com.example.basakhuji;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basakhuji.Adapter.PropertyAdapter;
import com.example.basakhuji.Models.PropertyList;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private PropertyAdapter adapter;
    private SearchView searchView;
    private ArrayList<PropertyList> propertyList;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    DocumentSnapshot document;
    String propertyId;
    BottomNavigationView bottomNavigationView;

    private TextView fullnameTextView, emailTextView;
    private FirebaseAuth mAuth;
    private ListenerRegistration userListener;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Remove default toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = findViewById(R.id.recyclerview);
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        propertyList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the navigation header view to access the TextViews
        View headerView = navigationView.getHeaderView(0);
        fullnameTextView = headerView.findViewById(R.id.nav_header_fullname);
        emailTextView = headerView.findViewById(R.id.nav_header_email);

        AllPropertyList();

        // Fetch user details from Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            displayUserDetails(currentUser.getUid());
        }

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
            }
            return false;
        });

    }


    private void displayUserDetails(String userId) {
        userListener = db.collection("users").document(userId)
                .addSnapshotListener(this, (documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("MainActivity", "Error fetching user details", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");

                        fullnameTextView.setText(fullName);
                        emailTextView.setText(email);
                    }
                });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about_us) {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        else if (id == R.id.nav_edit_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userListener != null) {
            userListener.remove();
        }
    }

}
