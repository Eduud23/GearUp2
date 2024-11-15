package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShopsFragment extends Fragment implements ShopAdapter.OnShopClickListener {

    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList;
    private List<Shop> filteredShopList;
    private FirebaseFirestore db;
    private EditText searchBar;
    private ImageView backIcon;

    public ShopsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shops, container, false);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the search bar and back icon
        searchBar = view.findViewById(R.id.search_bar);
        backIcon = view.findViewById(R.id.icon_back);

        // Initialize lists
        shopList = new ArrayList<>();
        filteredShopList = new ArrayList<>();

        // Set up the adapter
        shopAdapter = new ShopAdapter(filteredShopList, this);
        recyclerView.setAdapter(shopAdapter);

        // Set up the back icon click listener
        backIcon.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });

        // Load data from Firestore
        loadShopsFromFirestore();

        // Set up the search bar functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterShops(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    private void loadShopsFromFirestore() {
        db.collection("sellers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String shopName = document.getString("shopName");
                            String address = document.getString("address");
                            String phone = document.getString("phone");
                            String sellerId = document.getId();

                            if (shopName != null && address != null && phone != null) {
                                Shop shop = new Shop(shopName, address, phone, sellerId);
                                shopList.add(shop);
                            }
                        }

                        filteredShopList.clear();
                        filteredShopList.addAll(shopList);
                        shopAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Failed to load shops", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterShops(String query) {
        filteredShopList.clear();
        if (query.isEmpty()) {
            filteredShopList.addAll(shopList);
        } else {
            for (Shop shop : shopList) {
                if (shop.getShopName().toLowerCase().contains(query.toLowerCase())) {
                    filteredShopList.add(shop);
                }
            }
        }
        shopAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShopClick(int position) {
        Shop clickedShop = filteredShopList.get(position);

        // Pass the seller's ID to the SellerShopActivity to view the seller's shop details
        Intent intent = new Intent(getContext(), SellerShopActivity.class);
        intent.putExtra("SELLER_ID", clickedShop.getSellerId());
        startActivity(intent);
    }
}
