package com.example.gearup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProductDetails extends AppCompatActivity {

    private EditText etProductName, etProductPrice, etProductDescription;
    private ImageView ivProductImage;
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
        ivProductImage = findViewById(R.id.iv_product_image);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btn_delete);

        product = getIntent().getParcelableExtra("PRODUCT");
        position = getIntent().getIntExtra("POSITION", -1);

        if (product != null) {
            etProductName.setText(product.getName());
            etProductPrice.setText(String.valueOf(product.getPrice()));
            etProductDescription.setText(product.getDescription());
            Glide.with(this).load(product.getImageUrl()).into(ivProductImage);
        }

        btnSave.setOnClickListener(v -> {
            String name = etProductName.getText().toString();
            String priceString = etProductPrice.getText().toString();
            String description = etProductDescription.getText().toString();

            if (!name.isEmpty() && !priceString.isEmpty()) {
                double price = Double.parseDouble(priceString);
                product.setName(name);
                product.setPrice(price);
                product.setDescription(description);

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
