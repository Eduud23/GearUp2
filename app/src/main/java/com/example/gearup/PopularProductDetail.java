package com.example.gearup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PopularProductDetail extends AppCompatActivity {

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
    }
}
