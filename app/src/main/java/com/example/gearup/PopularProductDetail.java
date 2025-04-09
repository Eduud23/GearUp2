package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopularProductDetail extends AppCompatActivity {

    private static final String TAG = "PopularProductDetail";
    private FirebaseFirestore db;
    private RecyclerView similarProductsRecyclerView;
    private PopularProductAdapter similarProductAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_product_detail);

        // Initialize views
        TextView titleTextView = findViewById(R.id.productTitle);
        TextView priceTextView = findViewById(R.id.productPrice);
        TextView conditionTextView = findViewById(R.id.productCondition);
        TextView locationTextView = findViewById(R.id.productLocation);
        TextView shippingTextView = findViewById(R.id.productShipping);
        TextView sellerTextView = findViewById(R.id.productSeller);
        ImageView productImageView = findViewById(R.id.productImage);
        Button openLinkButton = findViewById(R.id.openItemButton);
        similarProductsRecyclerView = findViewById(R.id.similarProductsRecyclerView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set up RecyclerView for similar products
        similarProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        similarProductAdapter = new PopularProductAdapter(new ArrayList<>(), product -> {
            // When a similar product is clicked, navigate to its details page
            Intent intent = new Intent(PopularProductDetail.this, PopularProductDetail.class);
            intent.putExtra("title", product.getTitle());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("itemUrl", product.getItemUrl());
            intent.putExtra("condition", product.getCondition());
            intent.putExtra("location", product.getLocation());
            intent.putExtra("shippingCost", product.getShippingCost());
            intent.putExtra("seller", product.getSeller());
            startActivity(intent);
        });
        similarProductsRecyclerView.setAdapter(similarProductAdapter);

        // Get product data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String price = intent.getStringExtra("price");
            String imageUrl = intent.getStringExtra("imageUrl");
            String itemUrl = intent.getStringExtra("itemUrl");
            String condition = intent.getStringExtra("condition");
            String location = intent.getStringExtra("location");
            String shippingCost = intent.getStringExtra("shippingCost");
            String seller = intent.getStringExtra("seller");

            titleTextView.setText(title);
            priceTextView.setText("Price: " + price);
            conditionTextView.setText("Condition: " + condition);
            locationTextView.setText("Location: " + location);
            shippingTextView.setText("Shipping: " + shippingCost);
            sellerTextView.setText("Seller: " + seller);

            Glide.with(this).load(imageUrl).into(productImageView);

            openLinkButton.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl));
                startActivity(browserIntent);
            });
        } else {
            Log.e(TAG, "❌ Error: No data received in PopularProductDetail.");
        }

        // Fetch similar products
        fetchSimilarProducts();
    }

    private void fetchSimilarProducts() {
        // Initialize Firestore with the custom FirebaseApp instance
        try {
            FirebaseApp thirdApp = FirebaseApp.getInstance("gearupdataThirdApp");
            db = FirebaseFirestore.getInstance(thirdApp); // Use the custom FirebaseApp
            Log.d(TAG, "✅ Connected to Firestore (gearupdataThirdApp)");

            // Fetch products
            db.collection("ebay_popular_product").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<PopularProduct> fetchedProducts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PopularProduct product = new PopularProduct(
                                        document.getString("title"),
                                        document.getString("price"),
                                        document.getString("image"),
                                        document.getString("link"),
                                        document.getString("condition"),
                                        document.getString("location"),
                                        String.valueOf(document.get("shipping")),
                                        String.valueOf(document.get("discount")),
                                        String.valueOf(document.get("rated")),
                                        document.getString("seller")
                                );
                                fetchedProducts.add(product);
                            }

                            // Log the number of products fetched
                            Log.d(TAG, "Fetched " + fetchedProducts.size() + " similar products");

                            // Shuffle the products
                            Collections.shuffle(fetchedProducts);

                            // Update RecyclerView with similar products
                            if (fetchedProducts.isEmpty()) {
                                Log.d(TAG, "No similar products found.");
                            } else {
                                similarProductAdapter.updateProducts(fetchedProducts);  // Ensure your adapter method is called
                                Log.d(TAG, "Similar products updated in RecyclerView.");
                            }
                        } else {
                            Log.e(TAG, "❌ Error fetching similar products", task.getException());
                        }
                    });

        } catch (IllegalStateException e) {
            Log.e(TAG, "❌ FirebaseApp 'gearupdataThirdApp' not found.", e);
        }
    }

}
