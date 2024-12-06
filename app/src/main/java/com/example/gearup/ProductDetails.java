package com.example.gearup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

public class ProductDetails extends AppCompatActivity {

    private EditText etProductName, etProductPrice, etProductDescription, etProductQuantity, etProductBrand, etProductYearModel;
    private Button btnSave, btnDelete;
    private Product product;
    private int position;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        etProductName = findViewById(R.id.et_product_name);
        etProductPrice = findViewById(R.id.et_product_price);
        etProductDescription = findViewById(R.id.et_product_description);
        etProductQuantity = findViewById(R.id.et_product_quantity);
        etProductBrand = findViewById(R.id.et_product_brand);
        etProductYearModel = findViewById(R.id.et_product_year_model);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btn_delete);

        product = getIntent().getParcelableExtra("PRODUCT");
        position = getIntent().getIntExtra("POSITION", -1);

        // Initialize ViewPager2
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        if (product != null) {
            etProductName.setText(product.getName());
            etProductPrice.setText(String.valueOf(product.getPrice()));
            etProductDescription.setText(product.getDescription());
            etProductQuantity.setText(String.valueOf(product.getQuantity()));
            etProductBrand.setText(product.getBrand()); // Assuming getBrand() exists
            etProductYearModel.setText(product.getYearModel()); // Assuming getYearModel() exists

            // Load images into ViewPager2
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(product.getImageUrls());
            viewPager.setAdapter(imageSliderAdapter);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed(); // Go back when the back button is clicked
        });


        btnSave.setOnClickListener(v -> {
            String name = etProductName.getText().toString();
            String priceString = etProductPrice.getText().toString();
            String description = etProductDescription.getText().toString();
            String quantityString = etProductQuantity.getText().toString();
            String brand = etProductBrand.getText().toString();
            String yearModel = etProductYearModel.getText().toString();

            if (!name.isEmpty() && !priceString.isEmpty() && !quantityString.isEmpty() && !brand.isEmpty() && !yearModel.isEmpty()) {
                double price = Double.parseDouble(priceString);
                int quantity = Integer.parseInt(quantityString);
                product.setName(name);
                product.setPrice(price);
                product.setDescription(description);
                product.setQuantity(quantity);
                product.setBrand(brand); // Assuming setBrand() exists
                product.setYearModel(yearModel); // Assuming setYearModel() exists

                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_PRODUCT", product);
                resultIntent.putExtra("POSITION", position);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("DELETE_PRODUCT", true);
            resultIntent.putExtra("POSITION", position);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
