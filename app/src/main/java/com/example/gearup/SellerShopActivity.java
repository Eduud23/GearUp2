package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    private TextView shopNameTextView, phoneNumberTextView, addressTextView, seeDetailsTextView;
    private RecyclerView productsRecyclerView;
    private SellerShopAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> fullProductList = new ArrayList<>(); // For filtering purposes
    private FirebaseFirestore db;
    private String sellerId;
    private ImageView categorySpinner;
    private ImageView messageIcon, profileImageView;
    private EditText searchEditText; // Search bar for product search
    private View profileAndMessageContainer; // The entire profile and message container

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop);

        // Initialize UI components
        profileAndMessageContainer = findViewById(R.id.profile_and_message_container); // Initialize the profile and message container
        shopNameTextView = findViewById(R.id.tv_shop_name);
        addressTextView = findViewById(R.id.tv_address);
        phoneNumberTextView = findViewById(R.id.tv_phone_number);
        profileImageView = findViewById(R.id.iv_profile_image);
        productsRecyclerView = findViewById(R.id.rv_products);
        categorySpinner = findViewById(R.id.spinner_category);
        messageIcon = findViewById(R.id.iv_message_icon);
        seeDetailsTextView = findViewById(R.id.tv_see_details);
        searchEditText = findViewById(R.id.et_search); // Initialize search bar

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
        seeDetailsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SellerShopActivity.this, SellerDetailsActivity.class);
            intent.putExtra("SELLER_ID", sellerId); // Only passing the sellerId
            startActivity(intent);
        });

        // Set up the category Spinner
        setupCategorySpinner();

        // Set up the search functionality
        setupSearchEditText();

        // Get current user ID from Firebase Authentication
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set the click listener for the message icon
        setupMessageIconClickListener();
    }

    private void setupCategorySpinner() {
        categorySpinner.setOnClickListener(v -> {
            String[] categories = {"All", "Central Components", "Body", "Connectors", "Peripherals"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Category");
            builder.setItems(categories, (dialog, which) -> {
                String selectedCategory = categories[which];
                loadSellerProducts(sellerId, selectedCategory);
            });

            builder.show();
        });
    }

    private void setupSearchEditText() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter products as user types
                filterProducts(s.toString());

                // Hide the profile and message container when the search is in progress
                if (s.length() > 0) {
                    profileAndMessageContainer.setVisibility(View.GONE); // Hide the profile and message container
                } else {
                    profileAndMessageContainer.setVisibility(View.VISIBLE); // Show it back when search is cleared
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : fullProductList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getBrand().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList); // Update the adapter with the filtered list
    }

    private void loadSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        shopNameTextView.setText(documentSnapshot.getString("shopName"));
                        phoneNumberTextView.setText(documentSnapshot.getString("phone"));
                        addressTextView.setText(documentSnapshot.getString("address"));

                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.gear).into(profileImageView);

                        // Get latitude & longitude
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");

                        // Set click listener on address to open map
                        addressTextView.setOnClickListener(v -> {
                            Intent intent = new Intent(SellerShopActivity.this, ShopPinLocation.class);
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            startActivity(intent);
                        });

                        // Sold & Review Count
                        Long soldCount = documentSnapshot.getLong("sold");
                        TextView soldCountTextView = findViewById(R.id.tv_sold_count);
                        soldCountTextView.setText(soldCount != null ? String.valueOf(soldCount) : "0");

                        db.collection("sellers").document(sellerId).collection("review")
                                .get()
                                .addOnSuccessListener(reviewSnapshots -> {
                                    int reviewCount = reviewSnapshots.size();
                                    TextView reviewCountTextView = findViewById(R.id.tv_review_count);
                                    reviewCountTextView.setText(String.valueOf(reviewCount));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(this, "Shop not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting shop info", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSellerProducts(String sellerId, String category) {
        if (category.equals("All")) {
            db.collection("users").document(sellerId)
                    .collection("products")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        productList.clear();
                        fullProductList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId());
                                productList.add(product);
                                fullProductList.add(product); // Add to full list for filtering
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error getting products", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Error getting products", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }

    private void setupMessageIconClickListener() {
        messageIcon.setOnClickListener(v -> {
            if (sellerId != null && currentUserId != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("SELLER_ID", sellerId);
                intent.putExtra("CURRENT_USER_ID", currentUserId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Error: Missing seller or user ID", Toast.LENGTH_SHORT).show();
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
