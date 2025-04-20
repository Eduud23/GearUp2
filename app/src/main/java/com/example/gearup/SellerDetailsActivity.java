package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SellerDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView shopNameTextView, phoneNumberTextView, addressTextView, servicesTextView, soldTextView, reviewTextView, goNowButton;
    private ImageView profileImageView;
    private FirebaseFirestore db;
    private String sellerId;
    private GoogleMap mMap;

    // Variables to hold latitude and longitude
    private double sellerLatitude = 0.0;
    private double sellerLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_details);

        // Initialize UI components
        shopNameTextView = findViewById(R.id.tv_shop_name);
        phoneNumberTextView = findViewById(R.id.tv_phone_number);
        addressTextView = findViewById(R.id.tv_address);
        servicesTextView = findViewById(R.id.tv_services);
        soldTextView = findViewById(R.id.tv_sold);
        reviewTextView = findViewById(R.id.tv_review);
        profileImageView = findViewById(R.id.iv_profile_image);

        goNowButton = findViewById(R.id.goNow); // Find the "Go Now" TextView

        // Set OnClickListener for "Go Now" button
        goNowButton.setOnClickListener(v -> {
            if (sellerLatitude != 0.0 && sellerLongitude != 0.0) {
                Intent intent = new Intent(SellerDetailsActivity.this, ShopPinLocation.class);
                intent.putExtra("latitude", sellerLatitude);
                intent.putExtra("longitude", sellerLongitude);
                startActivity(intent);
            } else {
                Toast.makeText(SellerDetailsActivity.this, "Seller location is not available", Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        sellerId = getIntent().getStringExtra("SELLER_ID");

        if (sellerId != null && !sellerId.isEmpty()) {
            loadSellerDetails(sellerId);
        } else {
            Toast.makeText(this, "Seller ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadSellerDetails(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Display seller info
                        shopNameTextView.setText(documentSnapshot.getString("shopName"));
                        phoneNumberTextView.setText(documentSnapshot.getString("phone"));
                        addressTextView.setText(documentSnapshot.getString("address"));
                        servicesTextView.setText(documentSnapshot.getString("services"));
                        soldTextView.setText(String.valueOf(documentSnapshot.getLong("sold")));
                        reviewTextView.setText(String.valueOf(documentSnapshot.getLong("review")));

                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        Glide.with(this).load(profileImageUrl).into(profileImageView);

                        // Get latitude and longitude for map
                        sellerLatitude = documentSnapshot.getDouble("latitude");
                        sellerLongitude = documentSnapshot.getDouble("longitude");

                        // Update map
                        LatLng sellerLocation = new LatLng(sellerLatitude, sellerLongitude);
                        if (mMap != null) {
                            mMap.addMarker(new MarkerOptions().position(sellerLocation).title("Shop Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLocation, 15));
                        }

                    } else {
                        Toast.makeText(this, "Shop not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting shop details", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
