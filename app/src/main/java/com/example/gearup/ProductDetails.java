package com.example.gearup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProductDetails extends AppCompatActivity {

    private EditText productName, productPrice, productDescription;
    private ImageView productImage;
    private Button btnUpdate, btnDelete;
    private Product product;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productName = findViewById(R.id.et_product_name);
        productPrice = findViewById(R.id.et_product_price);
        productDescription = findViewById(R.id.et_product_description);
        productImage = findViewById(R.id.iv_product_image);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        product = getIntent().getParcelableExtra("PRODUCT");
        position = getIntent().getIntExtra("POSITION", -1);

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.valueOf(product.getPrice()));
            productDescription.setText(product.getDescription());
            Glide.with(this).load(product.getImageUri()).into(productImage);
        }

        btnUpdate.setOnClickListener(v -> updateProduct());
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void updateProduct() {
        String name = productName.getText().toString();
        String priceString = productPrice.getText().toString();
        String description = productDescription.getText().toString();

        if (name.isEmpty() || priceString.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceString);

        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_PRODUCT", product);
        resultIntent.putExtra("POSITION", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProduct())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteProduct() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("DELETE_PRODUCT", true);
        resultIntent.putExtra("POSITION", position);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
