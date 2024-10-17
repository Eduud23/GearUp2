package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductDetailsBuyer extends AppCompatActivity {
    private TextView productName, productPrice, productDescription, availableQuantityText, sellerName;
    private Button addToCartButton, checkoutButton;
    private EditText productQuantity;
    private ViewPager2 viewPager;
    private ImageView sellerProfileImage;
    private int maxQuantity;
    private String sellerId;

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details_buyer);

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

        db = FirebaseFirestore.getInstance();

        // Get product from intent
        Product product = getIntent().getParcelableExtra("PRODUCT");

        if (product != null) {
            productName.setText(product.getName());
            productPrice.setText(String.format("₱%.2f", product.getPrice()));
            productDescription.setText(product.getDescription());
            maxQuantity = product.getQuantity();
            availableQuantityText.setText("Available Quantity: " + maxQuantity);

            // Load images into ViewPager2
            ImageSliderAdapter imageSliderAdapter = new ImageSliderAdapter(product.getImageUrls());
            viewPager.setAdapter(imageSliderAdapter);

            // Retrieve seller info from Firestore
            sellerId = product.getSellerId();
            getSellerInfo(sellerId);
        }

        // Set up button click listeners
        addToCartButton.setOnClickListener(v -> addToCart(product));
        checkoutButton.setOnClickListener(v -> {
            // Handle checkout action
        });

        // Set up click listeners for product name and seller profile image
        productName.setOnClickListener(v -> openSellerShop());
        sellerProfileImage.setOnClickListener(v -> openSellerShop());
    }

    private void getSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
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

            boolean alreadyInCart = false;
            for (CartItem item : Cart.getInstance().getItems()) {
                if (item.getProduct().getId().equals(product.getId())) {
                    item.setQuantity(item.getQuantity() + quantity);
                    alreadyInCart = true;
                    break;
                }
            }

            if (!alreadyInCart) {
                Cart.getInstance().addToCart(product, quantity);
            }

            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProductDetailsBuyer.this, CartActivity.class);
            startActivity(intent);
        }
    }
}
