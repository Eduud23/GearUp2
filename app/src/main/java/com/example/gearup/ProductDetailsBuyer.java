package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsBuyer extends AppCompatActivity {
    private TextView productName, productPrice, productDescription, availableQuantityText, sellerName, productBrand, productYearModel;
    private Button addToCartButton, checkoutButton, addReviewButton;
    private EditText productQuantity;
    private ViewPager2 viewPager;
    private ImageView sellerProfileImage;
    private RecyclerView rvReviews;
    private int maxQuantity;
    private String sellerId;
    private String currentUserId;
    private Product product;

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details_buyer);

        // Initialize views
        productName = findViewById(R.id.tv_product_name);
        productPrice = findViewById(R.id.tv_product_price);
        productDescription = findViewById(R.id.tv_product_description);
        availableQuantityText = findViewById(R.id.tv_available_quantity);
        sellerName = findViewById(R.id.tv_seller_name);
        sellerProfileImage = findViewById(R.id.iv_seller_profile);
        addToCartButton = findViewById(R.id.btn_add_to_cart);
        checkoutButton = findViewById(R.id.btn_checkout);
        productQuantity = findViewById(R.id.et_product_quantity);
        viewPager = findViewById(R.id.viewPager);
        addReviewButton = findViewById(R.id.btn_add_review);
        rvReviews = findViewById(R.id.rv_reviews);

        productBrand = findViewById(R.id.tv_product_brand);
        productYearModel = findViewById(R.id.tv_product_year_model);

        db = FirebaseFirestore.getInstance();

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get product from intent
        product = getIntent().getParcelableExtra("PRODUCT");

        if (product != null && product.getId() != null) {
            Log.d("ProductDetailsBuyer", "Product ID: " + product.getId());
            setProductDetails();

            // Retrieve seller info from Firestore
            sellerId = product.getSellerId();
            getSellerInfo(sellerId);
            loadReviews(product.getId());
        } else {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up button click listeners
        addToCartButton.setOnClickListener(v -> addToCart(product));
        checkoutButton.setOnClickListener(v -> checkout());

        productName.setOnClickListener(v -> openSellerShop());
        sellerProfileImage.setOnClickListener(v -> openSellerShop());

        addReviewButton.setOnClickListener(v -> showReviewDialog());

        // Setup RecyclerView
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setProductDetails() {
        productName.setText(product.getName());
        productPrice.setText(String.format("₱%.2f", product.getPrice()));
        productDescription.setText(product.getDescription());
        maxQuantity = product.getQuantity();
        availableQuantityText.setText("Available Quantity: " + maxQuantity);
        productBrand.setText(product.getBrand());
        productYearModel.setText(product.getYearModel());

        // Load images into ViewPager2
        ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(product.getImageUrls());
        viewPager.setAdapter(imageSliderAdapter);
    }

    private void getSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String sellerNameStr = documentSnapshot.getString("shopName");
                        String sellerProfileImageUrl = documentSnapshot.getString("profileImageUrl");

                        sellerName.setText(sellerNameStr);
                        Glide.with(ProductDetailsBuyer.this)
                                .load(sellerProfileImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(sellerProfileImage);
                    } else {
                        Toast.makeText(ProductDetailsBuyer.this, "Seller not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ProductDetailsBuyer.this, "Error getting seller info", Toast.LENGTH_SHORT).show());
    }

    private void openSellerShop() {
        Intent intent = new Intent(ProductDetailsBuyer.this, SellerShopActivity.class);
        intent.putExtra("SELLER_ID", sellerId);
        startActivity(intent);
    }

    private void addToCart(Product product) {
        if (product != null) {
            String quantityText = productQuantity.getText().toString();
            int quantity = quantityText.isEmpty() ? 1 : Integer.parseInt(quantityText);

            if (quantity < 1 || quantity > maxQuantity) {
                Toast.makeText(this, "Please enter a quantity between 1 and " + maxQuantity, Toast.LENGTH_SHORT).show();
                return;
            }

            CartItem existingItem = null; // Declare a variable to hold the existing cart item
            for (CartItem item : Cart.getInstance().getItems()) {
                if (item.getProduct().getId().equals(product.getId())) {
                    existingItem = item; // Store the existing item if found
                    break;
                }
            }

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity); // Update the quantity
                updateCartItemInFirestore(product, existingItem.getQuantity()); // Update Firestore with the new quantity
            } else {
                Cart.getInstance().addToCart(product, quantity);
                saveCartItemToFirestore(product, quantity); // Add the new item to Firestore
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCartItemToFirestore(Product product, int quantity) {
        CartItem cartItem = new CartItem(product, quantity);
        db.collection("carts").add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ProductDetailsBuyer", "Cart item added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCartItemInFirestore(Product product, int quantity) {
        String existingCartItemId = getExistingCartItemId(product.getId());
        if (existingCartItemId != null) {
            db.collection("carts").document(existingCartItemId)
                    .update("quantity", quantity)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("ProductDetailsBuyer", "Cart item updated successfully");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update cart item", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getExistingCartItemId(String productId) {
        for (CartItem item : Cart.getInstance().getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                return item.getDocumentId();
            }
        }
        return null;
    }

    private void checkout() {
        Intent intent = new Intent(ProductDetailsBuyer.this, DeliveryInfoActivity.class);
        intent.putExtra("PRODUCT", product);
        intent.putExtra("PRODUCT_PRICE", product.getPrice());
        intent.putExtra("PRODUCT_QUANTITY", Integer.parseInt(productQuantity.getText().toString()));
        intent.putExtra("PRODUCT_NAME", product.getName());
        intent.putExtra("PRODUCT_BRAND", product.getBrand());
        intent.putExtra("PRODUCT_YEAR_MODEL", product.getYearModel());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            intent.putExtra("PRODUCT_IMAGE", product.getImageUrls().get(0));
        }

        startActivity(intent);
    }

    private void showReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_review, null);
        EditText editTextReview = dialogView.findViewById(R.id.editTextReview);
        Button buttonSubmitReview = dialogView.findViewById(R.id.buttonSubmitReview);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Add a Review")
                .setCancelable(true)
                .create();

        buttonSubmitReview.setOnClickListener(v -> {
            String reviewText = editTextReview.getText().toString().trim();
            if (!reviewText.isEmpty() && product != null && product.getId() != null) {
                submitReview(reviewText, product.getId());
                dialog.dismiss();
            } else {
                Toast.makeText(ProductDetailsBuyer.this, "Please enter a review", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void submitReview(String reviewText, String productId) {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review(reviewText, currentUserId);
        db.collection("productsreview")
                .document(productId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    loadReviews(productId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews(String productId) {
        db.collection("productsreview")
                .document(productId)
                .collection("reviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        reviews.add(review);
                    }
                    ReviewAdapter reviewsAdapter = new ReviewAdapter(reviews);
                    rvReviews.setAdapter(reviewsAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProductDetailsBuyer.this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                });
    }
}
