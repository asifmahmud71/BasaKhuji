package com.example.basakhuji;

import static com.android.volley.VolleyLog.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 10;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap myMap;
    private FrameLayout map;
    private FirebaseFirestore db;
    private Geocoder geocoder;

    // Map to store the number of markers for each LatLng
    private Map<LatLng, Integer> markerCountMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ImageView backButton = findViewById(R.id.backButton);
        map = findViewById(R.id.map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = FirebaseFirestore.getInstance();
        geocoder = new Geocoder(this, Locale.getDefault());

        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            finish(); // Finish the activity to prevent further execution
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myMap.setMyLocationEnabled(true);
        } else {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Initialize FusedLocationProviderClient
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Move the camera to the user's current location
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

                        // Add marker at current location
                        myMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error getting last known location", e);
                });

        // Fetch properties and add markers
        fetchPropertyMarkers();
    }

    private void fetchPropertyMarkers() {
        db.collection("properties")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String subArea = document.getString("subArea"); // Assuming "subArea" is the field name in Firestore
                            String area = document.getString("area"); // Assuming "area" is the field name in Firestore
                            String district = document.getString("district"); // Assuming "district" is the field name in Firestore

                            if (subArea != null && area != null && district != null) {
                                String address = subArea + ", " + area + ", " + district;
                                addMarkerForAddress(address);
                            }
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "Failed to fetch properties", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addMarkerForAddress(String address) {
        if (address != null && !address.isEmpty()) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address location = addresses.get(0);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    int offsetIndex = markerCountMap.getOrDefault(latLng, 0);
                    LatLng offsetLatLng = getOffsetLatLng(latLng, offsetIndex);

                    myMap.addMarker(new MarkerOptions()
                            .position(offsetLatLng)
                            .title(address)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.property_marker)));

                    // Update the marker count for this LatLng
                    markerCountMap.put(latLng, offsetIndex + 1);
                } else {
                    Log.e(TAG, "No location found for address: " + address);
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoder service error", e);
            }
        } else {
            Log.e(TAG, "Address is null or empty");
        }
    }

    private LatLng getOffsetLatLng(LatLng originalLatLng, int offsetIndex) {
        double offset = 0.0001 * offsetIndex; // Small offset
        return new LatLng(originalLatLng.latitude + offset, originalLatLng.longitude + offset);
    }
}
