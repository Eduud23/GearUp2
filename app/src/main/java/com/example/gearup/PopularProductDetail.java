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
import com.bumptech.glide.Glide;

public class PopularProductDetail extends AppCompatActivity {

    private static final String TAG = "PopularProductDetail";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_product_detail);

        TextView titleTextView = findViewById(R.id.productTitle);
        TextView priceTextView = findViewById(R.id.productPrice);
        TextView conditionTextView = findViewById(R.id.productCondition);
        TextView locationTextView = findViewById(R.id.productLocation);
        TextView shippingTextView = findViewById(R.id.productShipping);
        TextView sellerTextView = findViewById(R.id.productSeller); // ✅ Add this line
        ImageView productImageView = findViewById(R.id.productImage);
        Button openLinkButton = findViewById(R.id.openItemButton);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String price = intent.getStringExtra("price");
            String imageUrl = intent.getStringExtra("imageUrl");
            String itemUrl = intent.getStringExtra("itemUrl");
            String condition = intent.getStringExtra("condition");
            String location = intent.getStringExtra("location");
            String shippingCost = intent.getStringExtra("shippingCost");
            String seller = intent.getStringExtra("seller"); // ✅ Retrieve Seller

            titleTextView.setText(title);
            priceTextView.setText("Price: " + price);
            conditionTextView.setText("Condition: " + condition);
            locationTextView.setText("Location: " + location);
            shippingTextView.setText("Shipping: " + shippingCost);
            sellerTextView.setText("Seller: " + seller); // ✅ Display Seller

            Glide.with(this).load(imageUrl).into(productImageView);

            openLinkButton.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl));
                startActivity(browserIntent);
            });
        } else {
            Log.e(TAG, "❌ Error: No data received in PopularProductDetail.");
        }
    }
}
