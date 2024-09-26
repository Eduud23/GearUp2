package com.example.gearup;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentBuyer extends Fragment implements ProductAdapterBuyer.OnProductClickListener {
    private FirebaseFirestore db;
    private ViewPager2 viewPagerCentralComponents, viewPagerBody, viewPagerConnectors, viewPagerPeripherals;
    private ProductAdapterBuyer adapterCentralComponents, adapterBody, adapterConnectors, adapterPeripherals;
    private List<Product> centralComponentsList = new ArrayList<>();
    private List<Product> bodyList = new ArrayList<>();
    private List<Product> connectorsList = new ArrayList<>();
    private List<Product> peripheralsList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_buyer, container, false);

        viewPagerCentralComponents = view.findViewById(R.id.viewPager_central_components);
        viewPagerBody = view.findViewById(R.id.viewPager_body);
        viewPagerConnectors = view.findViewById(R.id.viewPager_connectors);
        viewPagerPeripherals = view.findViewById(R.id.viewPager_peripherals);

        db = FirebaseFirestore.getInstance();
        loadProducts();

        return view;
    }

    private void loadProducts() {
        db.collectionGroup("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        centralComponentsList.clear();
                        bodyList.clear();
                        connectorsList.clear();
                        peripheralsList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                categorizeProduct(product);
                                loadSellerProfile(product);
                                Log.d("HomeFragmentBuyer", "Loaded product: " + product.getName());
                            }
                        }

                        setAdapters();
                    } else {
                        Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void categorizeProduct(Product product) {
        switch (product.getCategory()) {
            case "Central Components":
                centralComponentsList.add(product);
                break;
            case "Body":
                bodyList.add(product);
                break;
            case "Connectors":
                connectorsList.add(product);
                break;
            case "Peripherals":
                peripheralsList.add(product);
                break;
        }
    }

    private void setAdapters() {
        adapterCentralComponents = new ProductAdapterBuyer(centralComponentsList, this);
        viewPagerCentralComponents.setAdapter(adapterCentralComponents);
        viewPagerCentralComponents.setOffscreenPageLimit(1);

        adapterBody = new ProductAdapterBuyer(bodyList, this);
        viewPagerBody.setAdapter(adapterBody);
        viewPagerBody.setOffscreenPageLimit(1);

        adapterConnectors = new ProductAdapterBuyer(connectorsList, this);
        viewPagerConnectors.setAdapter(adapterConnectors);
        viewPagerConnectors.setOffscreenPageLimit(1);

        adapterPeripherals = new ProductAdapterBuyer(peripheralsList, this);
        viewPagerPeripherals.setAdapter(adapterPeripherals);
        viewPagerPeripherals.setOffscreenPageLimit(1);

        Log.d("HomeFragmentBuyer", "Total products loaded: " + (centralComponentsList.size() + bodyList.size() + connectorsList.size() + peripheralsList.size()));
    }

    private void loadSellerProfile(Product product) {
        String sellerId = product.getSellerId();
        if (sellerId != null) {
            db.collection("sellers").document(sellerId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String profileImageUrl = document.getString("profileImageUrl");
                        product.setSellerProfileImageUrl(profileImageUrl);
                        notifyAdapters();
                    }
                }
            });
        }
    }

    private void notifyAdapters() {
        adapterCentralComponents.notifyDataSetChanged();
        adapterBody.notifyDataSetChanged();
        adapterConnectors.notifyDataSetChanged();
        adapterPeripherals.notifyDataSetChanged();
    }

    @Override
    public void onProductClick(int position) {
        // Handle product click
        // Determine which category the clicked product belongs to
        // Example:
        if (position < centralComponentsList.size()) {
            Product clickedProduct = centralComponentsList.get(position);
            Toast.makeText(getContext(), "Clicked: " + clickedProduct.getName(), Toast.LENGTH_SHORT).show();
        }
        // Add similar checks for other categories
    }
}
