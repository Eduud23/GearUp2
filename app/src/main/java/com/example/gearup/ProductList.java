package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductList extends AppCompatActivity {

    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> products = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;
    private TextView categoryHeader; // TextView for the category name header

    static final int PRODUCT_DETAILS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Initialize views
        recyclerViewProducts = findViewById(R.id.recyclerView_products);
        categoryHeader = findViewById(R.id.tv_category_header); // Find the TextView for category name

        // Set up RecyclerView
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(this, 2));

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Retrieve the category name from the Intent
        String category = getIntent().getStringExtra("CATEGORY");

        if (category != null) {
            // Set the category name in the header TextView
            categoryHeader.setText(category);
        } else {
            categoryHeader.setText("No Category Selected");
        }

        if (user != null) {
            loadProductsFromFirestore(category);
        } else {
            // Handle user not authenticated
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        productAdapter = new ProductAdapter(products, this::onProductClick);
        recyclerViewProducts.setAdapter(productAdapter);
    }

    private void loadProductsFromFirestore(String category) {
        db.collection("users")
                .document(user.getUid())
                .collection("products")
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            products.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Product product = document.toObject(Product.class);
                                if (product != null) {
                                    product.setId(document.getId()); // Set the document ID as the product ID

                                    // Get the sellerId from the product
                                    String sellerId = product.getSellerId();

                                    // Fetch the seller's profile image from the 'sellers' collection
                                    if (sellerId != null && !sellerId.isEmpty()) {
                                        fetchSellerProfileImage(sellerId, product);
                                    }

                                    products.add(product);
                                }
                            }
                            productAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle failure
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchSellerProfileImage(String sellerId, Product product) {
        db.collection("sellers")
                .document(sellerId) // Fetch the seller's document by sellerId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            product.setSellerProfileImageUrl(profileImageUrl);
                            productAdapter.notifyDataSetChanged(); // Notify adapter that the profile image URL is updated
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure to fetch seller's profile image
                    Toast.makeText(ProductList.this, "Failed to load seller profile image", Toast.LENGTH_SHORT).show();
                });
    }


    private void onProductClick(int position) {
        Intent intent = new Intent(this, ProductDetails.class);
        intent.putExtra("PRODUCT", products.get(position));
        intent.putExtra("POSITION", position);
        startActivityForResult(intent, PRODUCT_DETAILS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PRODUCT_DETAILS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            boolean deleteProduct = data.getBooleanExtra("DELETE_PRODUCT", false);
            if (deleteProduct) {
                int position = data.getIntExtra("POSITION", -1);
                if (position != -1) {
                    deleteProductFromFirestore(products.get(position).getId(), position);
                }
            } else {
                Product updatedProduct = data.getParcelableExtra("UPDATED_PRODUCT");
                int position = data.getIntExtra("POSITION", -1);
                if (updatedProduct != null && position != -1) {
                    updateProductInFirestore(updatedProduct, position);
                }
            }
        }
    }

    private void updateProductInFirestore(Product product, int position) {
        db.collection("users").document(user.getUid())
                .collection("products").document(product.getId())
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    products.set(position, product);
                    productAdapter.notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProductFromFirestore(String productId, int position) {
        db.collection("users").document(user.getUid())
                .collection("products").document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    products.remove(position);
                    productAdapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                });
    }
}
