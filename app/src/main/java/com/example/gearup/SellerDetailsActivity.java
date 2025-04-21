package com.example.gearup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        goNowButton = findViewById(R.id.goNow);
        profileImageView = findViewById(R.id.iv_profile_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();
        sellerId = getIntent().getStringExtra("SELLER_ID");

        if (sellerId != null && !sellerId.isEmpty()) {
            loadSellerDetails(sellerId);
        } else {
            Toast.makeText(this, "Seller ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        findViewById(R.id.tv_add_review).setOnClickListener(v -> showReviewDialog());
    }

    private void loadSellerDetails(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        shopNameTextView.setText(documentSnapshot.getString("shopName"));
                        phoneNumberTextView.setText(documentSnapshot.getString("phone"));
                        addressTextView.setText(documentSnapshot.getString("address"));
                        servicesTextView.setText(documentSnapshot.getString("services"));
                        soldTextView.setText(String.valueOf(documentSnapshot.getLong("sold")));

                        // Display average rating
                        Double avgRating = documentSnapshot.getDouble("review");
                        if (avgRating != null) {
                            reviewTextView.setText(String.format("%.1f", avgRating));
                        }

                        sellerLatitude = documentSnapshot.getDouble("latitude");
                        sellerLongitude = documentSnapshot.getDouble("longitude");

                        LatLng sellerLocation = new LatLng(sellerLatitude, sellerLongitude);
                        if (mMap != null) {
                            mMap.addMarker(new MarkerOptions().position(sellerLocation).title("Shop Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sellerLocation, 15));
                        }

                        // Load profile image
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);
                        }

                    } else {
                        Toast.makeText(this, "Shop not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting shop details", Toast.LENGTH_SHORT).show());
    }

    private void showReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editTextReview = dialogView.findViewById(R.id.editTextReview); // Get reference to EditText for review text
        Button buttonSubmitReview = dialogView.findViewById(R.id.buttonSubmitReview);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Rate This Seller")
                .setCancelable(true)
                .create();

        buttonSubmitReview.setOnClickListener(v -> {
            double rating = ratingBar.getRating();
            String reviewText = editTextReview.getText().toString().trim(); // Get review text

            if (rating == 0 && reviewText.isEmpty()) {
                Toast.makeText(SellerDetailsActivity.this, "Please provide a rating or write a review", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch the seller's first and last name
            fetchSellerFullNameAndSubmitReview(rating, reviewText, dialog);
        });

        dialog.show();
    }

    private void fetchSellerFullNameAndSubmitReview(double rating, String reviewText, AlertDialog dialog) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(SellerDetailsActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = currentUser.getUid();
                    String profileImageUrl = currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null;

                    // Add the new review using the seller's full name and user information
                    addNewReview(userId, rating, reviewText, fullName, profileImageUrl, dialog);
                })
                .addOnFailureListener(e -> Toast.makeText(SellerDetailsActivity.this, "Failed to fetch seller details", Toast.LENGTH_SHORT).show());
    }

    private void addNewReview(String userId, double rating, String reviewText, String fullName, String profileImageUrl, AlertDialog dialog) {
        // Create a new Timestamp for when the review is made
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() / 1000, 0);

        // Create the new review object
        Review newReview = new Review(
                reviewText,       // reviewText
                rating,           // starRating
                userId,           // userId
                fullName,         // fullName
                profileImageUrl,  // profileImageUrl
                timestamp         // timestamp
        );

        DocumentReference sellerRef = db.collection("sellers").document(sellerId);

        // Add the new review to Firestore
        sellerRef.collection("reviews").add(newReview)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(SellerDetailsActivity.this, "Review added", Toast.LENGTH_SHORT).show();
                    updateSellerAverageRating();  // Update the average rating after adding the review
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SellerDetailsActivity.this, "Failed to add review", Toast.LENGTH_SHORT).show();
                });

        dialog.dismiss(); // Dismiss the dialog after the review is submitted
    }

    private void updateSellerAverageRating() {
        DocumentReference sellerRef = db.collection("sellers").document(sellerId);

        sellerRef.collection("reviews").get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalReviews = querySnapshot.size();
                    double totalRating = 0;

                    for (DocumentSnapshot document : querySnapshot) {
                        Double rating = document.getDouble("starRating");
                        if (rating != null) {
                            totalRating += rating;
                        }
                    }

                    double averageRating = totalReviews > 0 ? totalRating / totalReviews : 0;

                    sellerRef.update("review", averageRating)
                            .addOnSuccessListener(aVoid -> {
                                reviewTextView.setText(String.format("%.1f", averageRating));
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SellerDetailsActivity.this, "Failed to update seller rating", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to calculate average rating", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
