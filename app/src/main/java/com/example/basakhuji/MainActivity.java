package com.example.basakhuji;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Property;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basakhuji.Adapter.PropertyAdapter;
import com.example.basakhuji.Models.PropertyList;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerview = findViewById(R.id.recyclerview);
        ArrayList<PropertyList> list = new ArrayList<>();
        list.add(new PropertyList(R.drawable.tolet, "Bachelor Seat"));
        list.add(new PropertyList(R.drawable.tolet, "Sublet Room"));
        list.add(new PropertyList(R.drawable.tolet, "Bachelor Room"));
        list.add(new PropertyList(R.drawable.tolet, "Family Flat"));
        list.add(new PropertyList(R.drawable.tolet, "Bachelor Seat"));
        list.add(new PropertyList(R.drawable.tolet, "Sublet Room"));
        list.add(new PropertyList(R.drawable.tolet, "Family Flat"));
        list.add(new PropertyList(R.drawable.tolet, "Bachelor Room"));
        list.add(new PropertyList(R.drawable.tolet, "Family Flat"));
        list.add(new PropertyList(R.drawable.tolet, "Bachelor Room"));
        list.add(new PropertyList(R.drawable.tolet, "Family Flat"));
        list.add(new PropertyList(R.drawable.tolet, "Bachelor Room"));




        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerview.setLayoutManager(gridLayoutManager);

        // Create adapter and set it to the RecyclerView
        PropertyAdapter adapter = new PropertyAdapter(list, this);
        recyclerview.setAdapter(adapter);


        // Initialize bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.favourite) {
                    Toast.makeText(MainActivity.this, "The Favourite Properties are listed here", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.add) {
                    // Handle add icon click
                    return true;
                } else if (item.getItemId() == R.id.profile) {
                    // Open ProfileActivity
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
}