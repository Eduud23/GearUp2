package com.example.gearup;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewsActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewAdapter reviewsAdapter;
    private FirebaseFirestore db;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_reviews);

        rvReviews = findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get product ID from intent
        productId = getIntent().getStringExtra("PRODUCT_ID");

        if (productId != null) {
            loadReviews(productId);
        } else {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadReviews(String productId) {
        db.collection("productsreview").document(productId).collection("reviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        reviews.add(review);
                    }
                    reviewsAdapter = new ReviewAdapter(reviews);
                    rvReviews.setAdapter(reviewsAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show());
    }
}
