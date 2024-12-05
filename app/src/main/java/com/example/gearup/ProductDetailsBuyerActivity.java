package com.example.gearup;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class ProductDetailsBuyerActivity extends AppCompatActivity {

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

        // Get the current user's ID and role
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            checkUserRole(currentUserId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Get product from the intent (via bundle or arguments)
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
        productPrice.setText(String.format("₱%.2f", product.getPrice()));
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
                Toast.makeText(this, "Please enter a review", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void submitReview(String reviewText, String productId) {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, get the user profile information before creating the review
        getUserProfileInfoForReview(reviewText, productId);
    }

    private void getUserProfileInfoForReview(final String reviewText, final String productId) {
        // Retrieve the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // First, check if the user is a buyer
        db.collection("buyers").document(userId).get()
                .addOnSuccessListener(buyerDoc -> {
                    if (buyerDoc.exists()) {
                        // If the user is a buyer, retrieve their firstName, lastName, and profileImageUrl
                        String firstName = buyerDoc.getString("firstName");
                        String lastName = buyerDoc.getString("lastName");
                        String profileImageUrl = buyerDoc.getString("profileImageUrl");

                        // Create the review with buyer's info
                        Review review = new Review(reviewText, userId, firstName + " " + lastName, profileImageUrl);

                        // Submit the review to Firestore
                        submitReviewToFirestore(review, productId);
                    } else {
                        // If not a buyer, check if the user is a seller
                        getSellerProfileInfoForReview(reviewText, productId, userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load buyer info", Toast.LENGTH_SHORT).show();
                });
    }

    private void getSellerProfileInfoForReview(final String reviewText, final String productId, final String userId) {
        // Check if the user is a seller
        db.collection("sellers").document(userId).get()
                .addOnSuccessListener(sellerDoc -> {
                    if (sellerDoc.exists()) {
                        // If the user is a seller, retrieve their shopName and profileImageUrl
                        String shopName = sellerDoc.getString("shopName");
                        String profileImageUrl = sellerDoc.getString("profileImageUrl");

                        // Create the review with seller's info
                        Review review = new Review(reviewText, userId, shopName, profileImageUrl);

                        // Submit the review to Firestore
                        submitReviewToFirestore(review, productId);
                    } else {
                        // Handle case where the user is neither a buyer nor a seller
                        Toast.makeText(this, "Unable to identify the user as buyer or seller", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load seller info", Toast.LENGTH_SHORT).show();
                });
    }

    private void submitReviewToFirestore(Review review, String productId) {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Store the review in Firestore
        db.collection("productsreview")  // Correct path to reviews collection
                .document(productId)
                .collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    loadReviews(productId);  // Reload reviews after submitting
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                });
    }


    private void loadReviews(String productId) {
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("productsreview")
                .document(productId)  // This points to the product document
                .collection("reviews")  // This points to the reviews collection within the product document
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        reviews.add(review);
                    }
                    // Set the RecyclerView adapter to display the reviews
                    ReviewAdapter reviewsAdapter = new ReviewAdapter(reviews);
                    rvReviews.setAdapter(reviewsAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                });
    }
}