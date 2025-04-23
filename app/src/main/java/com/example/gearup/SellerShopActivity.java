package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private TextView shopNameTextView, phoneNumberTextView, addressTextView, seeDetailsTextView, reviews;
    private RecyclerView productsRecyclerView;
    private SellerShopAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> fullProductList = new ArrayList<>(); // For filtering purposes
    private FirebaseFirestore db;
    private String selectedCategory = "All";

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
        reviews = findViewById(R.id.tv_review_count);
        searchEditText = findViewById(R.id.et_search); // Initialize search bar

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());

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

        setupCategorySpinner();
        selectedCategory = selectedCategory; // Save it when user picks one


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
                selectedCategory = categories[which]; // <- Update the selected category

                // Filter the current product list using the updated category AND current search query
                filterProducts(searchEditText.getText().toString());

                // Hide profile container if necessary
                if (!selectedCategory.equals("All") || searchEditText.getText().toString().trim().length() > 0) {
                    profileAndMessageContainer.setVisibility(View.GONE);
                } else {
                    profileAndMessageContainer.setVisibility(View.VISIBLE);
                }
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
                filterProducts(s.toString());

                // Hide container if typing
                if (s.length() > 0) {
                    profileAndMessageContainer.setVisibility(View.GONE);
                } else {
                    // Only show it if category is "All"
                    String currentCategory = getCurrentCategory(); // Helper method below
                    if (currentCategory.equals("All")) {
                        profileAndMessageContainer.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });
    }
    private String getCurrentCategory() {
        return selectedCategory != null ? selectedCategory : "All";
    }


    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        query = query != null ? query.toLowerCase().trim() : "";

        for (Product product : fullProductList) {
            String name = product.getName() != null ? product.getName().toLowerCase() : "";
            String brand = product.getBrand() != null ? product.getBrand().toLowerCase() : "";
            String category = product.getCategory() != null ? product.getCategory().toLowerCase() : "";

            boolean matchesQuery = name.contains(query) || brand.contains(query);
            boolean matchesCategory = selectedCategory.equals("All") ||
                    selectedCategory.equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                filteredList.add(product);
            }
        }

        productAdapter.updateList(filteredList);
    }

    private void loadSellerInfo(String sellerId) {
        Log.d("SellerInfo", "loadSellerInfo called for sellerId: " + sellerId);

        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch the review field (which is a number, not a subcollection)
                        Double reviewValue = documentSnapshot.getDouble("review");

                        // Log the review value to verify it's being fetched correctly
                        Log.d("Review", "Fetched review value: " + reviewValue);

                        // Check if reviewValue is not null, then round to 1 decimal place
                        if (reviewValue != null) {
                            // Round the review value to 1 decimal place
                            String formattedReview = String.format("%.1f ⭐", reviewValue);

                            // Update tv_review_count with the formatted review value
                            TextView reviewCountTextView = findViewById(R.id.tv_review_count);
                            reviewCountTextView.setText(formattedReview);  // Set the formatted review value here
                        } else {
                            TextView reviewCountTextView = findViewById(R.id.tv_review_count);
                            reviewCountTextView.setText("No reviews yet");  // Handle case if review is null
                        }

                        // Update other fields as usual
                        shopNameTextView.setText(documentSnapshot.getString("shopName"));
                        phoneNumberTextView.setText(documentSnapshot.getString("phone"));
                        addressTextView.setText(documentSnapshot.getString("address"));

                        // Update other UI elements
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        Glide.with(this).load(profileImageUrl).placeholder(R.drawable.gear).into(profileImageView);

                        // Get latitude & longitude for the map
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");

                        // Set click listener for address to open map
                        addressTextView.setOnClickListener(v -> {
                            Intent intent = new Intent(SellerShopActivity.this, ShopPinLocation.class);
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            startActivity(intent);
                        });

                        // Sold count
                        Long soldCount = documentSnapshot.getLong("sold");
                        TextView soldCountTextView = findViewById(R.id.tv_sold_count);
                        soldCountTextView.setText(soldCount != null ? String.valueOf(soldCount) : "0");

                    } else {
                        Toast.makeText(this, "Shop not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting shop info", Toast.LENGTH_SHORT).show();
                    Log.e("SellerInfo", "Error fetching shop info", e); // Log error
                });
    }


    private void loadSellerProducts(String sellerId, String category) {
        db.collection("users").document(sellerId)
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    fullProductList.clear(); // Always clear this

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null &&
                                (category.equals("All") || category.equalsIgnoreCase(product.getCategory()))) {
                            product.setId(document.getId());
                            productList.add(product);
                            fullProductList.add(product);
                        }
                    }

                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error getting products", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
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
