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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PopularProductDetail extends AppCompatActivity {

   /* private RecyclerView similarProductsRecyclerView;
    private PopularProductAdapter adapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "PopularProductDetail";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_product_detail);

        // Initialize views
        TextView titleTextView = findViewById(R.id.product_title);
        TextView priceTextView = findViewById(R.id.product_price);
        TextView conditionTextView = findViewById(R.id.product_condition);
        TextView locationTextView = findViewById(R.id.product_location);
        TextView shippingCostTextView = findViewById(R.id.product_shipping_cost);
        ImageView productImageView = findViewById(R.id.product_image);
        Button viewOnEbayButton = findViewById(R.id.view_on_ebay_button);
        similarProductsRecyclerView = findViewById(R.id.similar_products_recycler);

        // Setup RecyclerView with click listener
        adapter = new PopularProductAdapter(new ArrayList<>(), product -> {
            Intent detailIntent = new Intent(PopularProductDetail.this, PopularProductDetail.class);
            detailIntent.putExtra("title", product.getTitle());
            detailIntent.putExtra("price", product.getPrice());
            detailIntent.putExtra("imageUrl", product.getImageUrl());
            detailIntent.putExtra("itemUrl", product.getItemUrl());
            detailIntent.putExtra("condition", product.getCondition());
            detailIntent.putExtra("location", product.getLocation());
            detailIntent.putExtra("shippingCost", product.getShippingCost());
            startActivity(detailIntent);
        });

        similarProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        similarProductsRecyclerView.setAdapter(adapter);

        // Get intent data
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String price = intent.getStringExtra("price");
            String imageUrl = intent.getStringExtra("imageUrl");
            String itemUrl = intent.getStringExtra("itemUrl");
            String condition = intent.getStringExtra("condition");
            String location = intent.getStringExtra("location");
            String shippingCost = intent.getStringExtra("shippingCost");

            // Set data to views
            titleTextView.setText(title != null ? title : "No Title");
            priceTextView.setText(price != null ? price : "N/A");
            conditionTextView.setText("Condition: " + (condition != null ? condition : "Unknown"));
            locationTextView.setText("Location: " + (location != null ? location : "Not Specified"));
            shippingCostTextView.setText("Shipping: " + (shippingCost != null ? shippingCost : "Varies"));

            // Load image
            Glide.with(this).load(imageUrl).into(productImageView);

            // Open item in browser
            viewOnEbayButton.setOnClickListener(v -> {
                if (itemUrl != null && !itemUrl.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl));
                    startActivity(browserIntent);
                }
            });
        }

        // Fetch and display similar products
        fetchSimilarProducts();
    }

    private void fetchSimilarProducts() {
        executorService.execute(() -> {
            try {
                List<PopularProduct> similarProducts = ProductFetcher.fetchProducts();
                runOnUiThread(() -> {
                    adapter.updateProducts(similarProducts);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching similar products", e);
            }
        });
    }*/

}
