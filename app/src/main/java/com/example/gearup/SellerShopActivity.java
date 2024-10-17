package com.example.gearup;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SellerShopActivity extends AppCompatActivity {
    private TextView shopNameTextView;
    private RecyclerView productsRecyclerView;
    private SellerShopAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop);

        shopNameTextView = findViewById(R.id.tv_shop_name);
        productsRecyclerView = findViewById(R.id.rv_products);

        // Set GridLayoutManager with 3 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        productsRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize the adapter with the product list and a click listener
        productAdapter = new SellerShopAdapter(productList, new SellerShopAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(int position, String category) {
                // Handle product click event
                // For example, you could start a new activity to show product details
                Toast.makeText(SellerShopActivity.this, "Clicked on: " + productList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        productsRecyclerView.setAdapter(productAdapter);

        db = FirebaseFirestore.getInstance();
        sellerId = getIntent().getStringExtra("SELLER_ID");

        loadSellerInfo(sellerId);
        loadSellerProducts(sellerId);
    }

    private void loadSellerInfo(String sellerId) {
        db.collection("sellers").document(sellerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String shopName = documentSnapshot.getString("shopName");
                            shopNameTextView.setText(shopName);
                        } else {
                            Toast.makeText(SellerShopActivity.this, "Shop not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SellerShopActivity.this, "Error getting shop info", Toast.LENGTH_SHORT).show());
    }

    private void loadSellerProducts(String sellerId) {
        db.collection("users").document(sellerId)
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SellerShopActivity.this, "Error getting products", Toast.LENGTH_SHORT).show());
    }
}
