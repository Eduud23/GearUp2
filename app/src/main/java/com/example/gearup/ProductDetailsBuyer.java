package com.example.gearup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class ProductDetailsBuyer extends AppCompatActivity {
    private TextView productName, productPrice, productDescription, productQuantity;
    private ImageView productImage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details_buyer);

        productName = findViewById(R.id.tv_product_name);
        productPrice = findViewById(R.id.tv_product_price);
        productDescription = findViewById(R.id.tv_product_description);
        productQuantity = findViewById(R.id.tv_product_quantity);
        productImage = findViewById(R.id.iv_product_image);

        // Get product from intent
        Product product = getIntent().getParcelableExtra("PRODUCT");

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.format("$%.2f", product.getPrice()));
            productDescription.setText(product.getDescription());
            productQuantity.setText("Quantity: " + product.getQuantity());
            Glide.with(this).load(product.getImageUrl()).into(productImage);
        }
    }
}
