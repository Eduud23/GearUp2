package com.example.gearup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragmentBuyer extends Fragment implements ProductAdapter.OnProductClickListener {
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewProducts;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_buyer, container, false);

        recyclerViewProducts = view.findViewById(R.id.recyclerView_products);
        recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        productAdapter = new ProductAdapter(productList, this);
        recyclerViewProducts.setAdapter(productAdapter);

        loadProducts();

        return view;
    }

    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                productList.add(product);
                                loadSellerProfile(product); // Load seller profile for each product
                                Log.d("HomeFragmentBuyer", "Loaded product: " + product.getName());
                            }
                        }
                        productAdapter.notifyDataSetChanged(); // Notify adapter after loading products
                    } else {
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadSellerProfile(Product product) {
        String sellerId = product.getSellerId(); // Ensure the Product class has this method
        if (sellerId != null) {
            db.collection("sellers").document(sellerId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String profileImageUrl = document.getString("profileImageUrl");
                        product.setSellerProfileImageUrl(profileImageUrl); // Set profile image URL
                        productAdapter.notifyDataSetChanged(); // Notify adapter after updating seller info
                    }
                }
            });
        }
    }


    @Override
    public void onProductClick(int position) {
        Product clickedProduct = productList.get(position);
        Toast.makeText(getContext(), "Clicked: " + clickedProduct.getName(), Toast.LENGTH_SHORT).show();
    }
}
