package com.example.gearup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailsBuyerActivity extends AppCompatActivity {

    private TextView productName, productPrice, productDescription, availableQuantityText, sellerName, productBrand, productYearModel, tvAverageRating;
    private Button addToCartButton, checkoutButton, addReviewButton;
    private EditText productQuantity;
    private ListenerRegistration reviewsListener;
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
        tvAverageRating = findViewById(R.id.tv_average_rating);
        productBrand = findViewById(R.id.tv_product_brand);
        productYearModel = findViewById(R.id.tv_product_year_model);




        db = FirebaseFirestore.getInstance();


        // Get the current user's ID and role
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            checkUserRole(currentUserId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Get product from the intent
        if (getIntent() != null) {
            product = getIntent().getParcelableExtra("PRODUCT");
        }

        if (product != null && product.getId() != null) {
            setProductDetails();

            // Retrieve seller info from Firestore
            sellerId = product.getSellerId();
            getSellerInfo(sellerId);

            loadReviews(product.getId());
        } else {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up button click listeners
        addToCartButton.setOnClickListener(v -> addToCart(product));
        checkoutButton.setOnClickListener(v -> checkout());
        productName.setOnClickListener(v -> openSellerShop());
        sellerProfileImage.setOnClickListener(v -> openSellerShop());
        addReviewButton.setOnClickListener(v -> showReviewDialog());

        // Setup RecyclerView for reviews
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        AppCompatImageButton btnPrevious = findViewById(R.id.btn_previous);
        AppCompatImageButton btnNext = findViewById(R.id.btn_next);

        // Set up button listeners
        btnPrevious.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true); // Move to the previous image
            }
        });


        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            int totalItems = viewPager.getAdapter() != null ? viewPager.getAdapter().getItemCount() : 0;
            if (currentItem < totalItems - 1) {
                viewPager.setCurrentItem(currentItem + 1, true); // Move to the next image
            }
        });
    }
    private void checkUserRole(String userId) {
        db.collection("buyers").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Current user is a buyer
                    } else {
                        // Check if the user is a seller
                        db.collection("sellers").document(userId).get()
                                .addOnSuccessListener(doc -> {
                                    if (doc.exists()) {
                                        // Current user is a seller
                                    } else {
                                        Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check user role", Toast.LENGTH_SHORT).show();
                });
    }

    private void setProductDetails() {
        productName.setText(product.getName());
        productPrice.setText(String.format("₱%,.2f", product.getPrice()));
        productDescription.setText(product.getDescription());
        maxQuantity = product.getQuantity();
        availableQuantityText.setText("Available Quantity: " + maxQuantity);
        productBrand.setText(product.getBrand());
        productYearModel.setText(product.getYearModel());

        // Load images into ViewPager2 (check for empty image URLs)
        List<String> imageUrls = product.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(imageUrls);
            viewPager.setAdapter(imageSliderAdapter);
        } else {
            // Handle case where product has no images
            Toast.makeText(this, "No images available for this product", Toast.LENGTH_SHORT).show();
        }
    }

    private void getSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String sellerNameStr = documentSnapshot.getString("shopName");
                        String sellerProfileImageUrl = documentSnapshot.getString("profileImageUrl");

                        sellerName.setText(sellerNameStr);
                        Glide.with(this)
                                .load(sellerProfileImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(sellerProfileImage);
                    } else {
                        Toast.makeText(this, "Seller not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error getting seller info", Toast.LENGTH_SHORT).show());
    }

    private void openSellerShop() {
        Intent intent = new Intent(this, SellerShopActivity.class);
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

            CartItem existingItem = null;
            for (CartItem item : Cart.getInstance().getItems()) {
                if (item.getProduct().getId().equals(product.getId())) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                updateCartItemInFirestore(product, existingItem.getQuantity());
            } else {
                Cart.getInstance().addToCart(product, quantity);
                saveCartItemToFirestore(product, quantity);
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCartItemToFirestore(Product product, int quantity) {
        CartItem cartItem = new CartItem(product, quantity);
        db.collection("buyers").document(currentUserId).collection("cartItems").add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCartItemInFirestore(Product product, int quantity) {
        String existingCartItemId = getExistingCartItemId(product.getId());
        if (existingCartItemId != null) {
            db.collection("buyers").document(currentUserId).collection("cartItems").document(existingCartItemId)
                    .update("quantity", quantity)
                    .addOnSuccessListener(aVoid -> {
                        // Handle success
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
        Intent intent = new Intent(this, DeliveryInfoActivity.class);
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
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button buttonSubmitReview = dialogView.findViewById(R.id.buttonSubmitReview);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Add or Update Review")
                .setCancelable(true)
                .create();

        buttonSubmitReview.setOnClickListener(v -> {
            String reviewText = editTextReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            // Prevent submission if both fields are empty
            if (reviewText.isEmpty() && rating == 0) {
                Toast.makeText(this, "Please enter a review or select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (product != null && product.getId() != null && product.getSellerId() != null) {
                submitReview(reviewText, rating, product.getId(), product.getSellerId());
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid product or seller ID", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }



    private void submitReview(String reviewText, float rating, String productId, String sellerId) {
        if (productId == null || productId.isEmpty() || sellerId == null || sellerId.isEmpty()) {
            Toast.makeText(this, "Invalid product or seller ID", Toast.LENGTH_SHORT).show();
            return;
        }

        getUserProfileInfoForReview(reviewText, rating, productId, sellerId);
    }

    private void getUserProfileInfoForReview(final String reviewText, final float rating, final String productId, final String sellerId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("buyers").document(userId).get()
                .addOnSuccessListener(buyerDoc -> {
                    if (buyerDoc.exists()) {
                        String firstName = buyerDoc.getString("firstName");
                        String lastName = buyerDoc.getString("lastName");
                        String profileImageUrl = buyerDoc.getString("profileImageUrl");

                        Timestamp timestamp = Timestamp.now();

                        Review review = new Review(reviewText, rating, userId, firstName + " " + lastName, profileImageUrl, timestamp);
                        submitReviewToFirestore(review, productId, sellerId);
                    } else {
                        getSellerProfileInfoForReview(reviewText, rating, productId, sellerId, userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load buyer info", Toast.LENGTH_SHORT).show();
                });
    }

    private void getSellerProfileInfoForReview(final String reviewText, final float rating, final String productId, final String sellerId, final String userId) {
        db.collection("sellers").document(userId).get()
                .addOnSuccessListener(sellerDoc -> {
                    if (sellerDoc.exists()) {
                        String shopName = sellerDoc.getString("shopName");
                        String profileImageUrl = sellerDoc.getString("profileImageUrl");

                        Timestamp timestamp = Timestamp.now();

                        Review review = new Review(reviewText, rating, userId, shopName, profileImageUrl, timestamp);
                        submitReviewToFirestore(review, productId, sellerId);
                    } else {
                        Toast.makeText(this, "Unable to identify the user as buyer or seller", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load seller info", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitReviewToFirestore(final Review review, final String productId, final String sellerId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference reviewRef = db.collection("productsreview").document(productId)
                .collection("reviews").document(userId);

        reviewRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Update the existing review
                reviewRef.set(review, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Review updated successfully!", Toast.LENGTH_SHORT).show();
                            updateAverageRating(productId, sellerId);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show());
            }
            else {
                // Add a new review
                reviewRef.set(review)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                            updateAverageRating(productId, sellerId);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateAverageRating(String productId, String sellerId) {
        db.collection("productsreview").document(productId)
                .collection("reviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double rating = document.getDouble("starRating");
                        Log.d("FirestoreDebug", "Rating found: " + rating); // Debugging log
                        if (rating != null) {
                            totalRating += rating;
                            count++;
                        }
                    }

                    if (count > 0) {
                        double averageRating = totalRating / count;
                        Log.d("FirestoreDebug", "Calculated average: " + averageRating);
                        saveAverageRating(productId, sellerId, averageRating);
                        // Set the average rating to tvAverageRating TextView
                        tvAverageRating.setText(String.format("%.2f", averageRating));
                    } else {
                        Log.d("FirestoreDebug", "No ratings found");
                        tvAverageRating.setText("No ratings yet");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Failed to calculate average rating", e);
                });
    }

    private void saveAverageRating(String productId, String sellerId, double averageRating) {
        String sellerPath = "users/" + sellerId + "/products/" + productId;
        Log.d("FirestoreDebug", "Updating stars in path: " + sellerPath + " with " + averageRating);

        db.document(sellerPath)
                .update("stars", averageRating)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreDebug", "Rating successfully updated: " + averageRating);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "Failed to update rating", e);
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (product != null && product.getId() != null) {
            loadAverageRating(product.getId());  // Reload average rating
        }
    }

    private void loadAverageRating(String productId) {
        db.collection("productsreview")
                .document(productId)  // This points to the product document
                .collection("reviews")  // This points to the reviews collection
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = 0;
                    int count = 0;

                    // Loop through all reviews to calculate the total rating
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Double rating = document.getDouble("starRating");
                        if (rating != null) {
                            totalRating += rating;
                            count++;
                        }
                    }

                    // Calculate and display the average rating
                    if (count > 0) {
                        double averageRating = totalRating / count;
                        tvAverageRating.setText(String.format("%.2f", averageRating));
                    } else {
                        tvAverageRating.setText("No ratings yet");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load average rating", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews(String productId) {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove any existing listener to prevent duplicate listeners
        if (reviewsListener != null) {
            reviewsListener.remove();
        }

        // Set up a real-time listener
        reviewsListener = db.collection("productsreview")
                .document(productId)  // This points to the product document
                .collection("reviews")  // This points to the reviews collection
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order reviews by timestamp (newest first)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        reviews.add(review);
                    }

                    // Update the RecyclerView adapter dynamically
                    ReviewAdapter reviewsAdapter = new ReviewAdapter(reviews);
                    rvReviews.setAdapter(reviewsAdapter);
                });
    }

    // Call this method in onDestroy() to avoid memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reviewsListener != null) {
            reviewsListener.remove();
        }
    }
}