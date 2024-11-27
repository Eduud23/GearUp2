package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerShopActivity extends AppCompatActivity implements SellerShopAdapter.OnProductClickListener {

    private TextView shopNameTextView, phoneNumberTextView, addressTextView;
    private RecyclerView productsRecyclerView;
    private SellerShopAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private String sellerId;
    private Spinner categorySpinner;
    private ImageView messageIcon, profileImageView;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop);

        // Initialize UI components
        shopNameTextView = findViewById(R.id.tv_shop_name);
        addressTextView = findViewById(R.id.tv_address);
        phoneNumberTextView = findViewById(R.id.tv_phone_number);
        profileImageView = findViewById(R.id.iv_profile_image);
        productsRecyclerView = findViewById(R.id.rv_products);
        categorySpinner = findViewById(R.id.spinner_categories);
        messageIcon = findViewById(R.id.iv_message_icon);

        // Set GridLayoutManager with 2 columns for the RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize the adapter with the product list and a click listener
        productAdapter = new SellerShopAdapter(productList, this);
        productsRecyclerView.setAdapter(productAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the sellerId passed from the previous activity
        sellerId = getIntent().getStringExtra("SELLER_ID");

        if (sellerId != null && !sellerId.isEmpty()) {
            // Load seller information and products from Firestore
            loadSellerInfo(sellerId);
            loadSellerProducts(sellerId, "All");
        } else {
            Toast.makeText(this, "Seller ID not provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up the category Spinner
        setupCategorySpinner();

        // Get current user ID from Firebase Authentication
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set the click listener for the message icon
        setupMessageIconClickListener();
    }

    private void setupCategorySpinner() {
        String[] categories = {"All", "Central Components", "Body", "Connectors", "Peripherals"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                loadSellerProducts(sellerId, selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                loadSellerProducts(sellerId, "All");
            }
        });
    }

    private void loadSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Set shop name
                        String shopName = documentSnapshot.getString("shopName");
                        if (shopName != null && !shopName.isEmpty()) {
                            shopNameTextView.setText(shopName);
                        } else {
                            Toast.makeText(SellerShopActivity.this, "Shop name not found", Toast.LENGTH_SHORT).show();
                        }

                        // Set profile image
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(SellerShopActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.gear)  // Placeholder if image is missing
                                    .into(profileImageView);
                        }

                        // Set phone number
                        String phoneNumber = documentSnapshot.getString("phone");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            phoneNumberTextView.setText(phoneNumber);
                        }

                        // Set address
                        String address = documentSnapshot.getString("address");
                        if (address != null && !address.isEmpty()) {
                            addressTextView.setText(address); // Assuming you have a TextView for the address
                        } else {
                            addressTextView.setText("Address not available"); // Optional: Default message if no address found
                        }

                    } else {
                        Toast.makeText(SellerShopActivity.this, "Shop not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SellerShopActivity.this, "Error getting shop info", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void loadSellerProducts(String sellerId, String category) {
        if (category.equals("All")) {
            db.collection("users").document(sellerId)
                    .collection("products")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        productList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId());
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SellerShopActivity.this, "Error getting products", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        } else {
            db.collection("users").document(sellerId)
                    .collection("products")
                    .whereEqualTo("category", category)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        productList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId());
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SellerShopActivity.this, "Error getting products", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }

    private void setupMessageIconClickListener() {
        messageIcon.setOnClickListener(v -> {
            if (sellerId != null && currentUserId != null) {
                // Start the ChatActivity and pass both the sellerId and currentUserId
                Intent intent = new Intent(SellerShopActivity.this, ChatActivity.class);
                intent.putExtra("SELLER_ID", sellerId);
                intent.putExtra("CURRENT_USER_ID", currentUserId);  // Pass the current logged-in user ID
                startActivity(intent);
            } else {
                Toast.makeText(SellerShopActivity.this, "Error: Missing seller or current user ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null) {
            Intent intent = new Intent(this, ProductDetailsBuyerActivity.class);
            intent.putExtra("PRODUCT", product);
            startActivity(intent);
        }
    }
}
