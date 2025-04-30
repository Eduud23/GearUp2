package com.example.gearup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapFilterActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    private GoogleMap mMap;
    private Circle filterCircle;
    private LatLng selectedCenter;
    private float selectedRadius = 1000f; // Default radius in meters

    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    private SeekBar radiusSeekBar;
    private TextView radiusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_filter);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        radiusSeekBar = findViewById(R.id.seekbar_radius);
        radiusText = findViewById(R.id.tv_radius_value);
        radiusSeekBar.setMax(100); // 100 * 100 = 10km max
        radiusSeekBar.setProgress(10); // default 1000m
        updateRadiusText(1000);

        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedRadius = progress * 100;
                updateRadiusText(selectedRadius);
                if (selectedCenter != null) {
                    drawFilterCircle(selectedCenter, selectedRadius);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button confirmButton = findViewById(R.id.btn_confirm);
        confirmButton.setOnClickListener(v -> {
            if (selectedCenter != null) {
                Intent result = new Intent();
                result.putExtra("center_lat", selectedCenter.latitude);
                result.putExtra("center_lng", selectedCenter.longitude);
                result.putExtra("radius", selectedRadius);

                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void updateRadiusText(float radius) {
        radiusText.setText("Radius: " + (int) radius + " meters");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                selectedCenter = userLatLng;
                drawFilterCircle(userLatLng, selectedRadius);
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });

        // Load sellers' locations on the map
        loadSellersOnMap();

        mMap.setOnMapClickListener(latLng -> {
            selectedCenter = latLng;
            drawFilterCircle(latLng, selectedRadius);
        });
    }

    private void drawFilterCircle(LatLng center, float radius) {
        if (filterCircle != null) {
            filterCircle.remove();
        }

        filterCircle = mMap.addCircle(new CircleOptions()
                .center(center)
                .radius(radius)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF) // Semi-transparent blue
                .strokeWidth(2f));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 13));
    }

    private void loadSellersOnMap() {
        db.collection("sellers").get().addOnSuccessListener(query -> {
            for (DocumentSnapshot doc : query.getDocuments()) {
                Double lat = doc.getDouble("latitude");
                Double lng = doc.getDouble("longitude");
                String name = doc.getString("name");
                String profileImageUrl = doc.getString("profileImageUrl");

                if (lat != null && lng != null && profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    LatLng sellerLatLng = new LatLng(lat, lng);

                    Glide.with(this)
                            .asBitmap()
                            .load(profileImageUrl)
                            .circleCrop() // optional: make it a circle
                            .into(new CustomTarget<Bitmap>(100, 100) {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(resource);

                                    mMap.addMarker(new MarkerOptions()
                                            .position(sellerLatLng)
                                            .title(name != null ? name : "Seller")
                                            .icon(icon));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {}
                            });
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load sellers", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            } else {
                Toast.makeText(this, "Location permission is required to use the map", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
