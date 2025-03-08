package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class SellerShopsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_shops, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewSellerShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        shopList = new ArrayList<>();
        shopAdapter = new ShopAdapter(shopList, sellerId -> {
            Intent intent = new Intent(getContext(), SellerShopActivity.class);
            intent.putExtra("SELLER_ID", sellerId); // Pass seller ID to new activity
            startActivity(intent);
        }, requireContext());

        recyclerView.setAdapter(shopAdapter);
        loadShopsFromFirestore();

        return view;
    }

    private void loadShopsFromFirestore() {
        db.collection("sellers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String shopName = document.getString("shopName");
                    String address = document.getString("address");
                    String phone = document.getString("phone");
                    String sellerId = document.getId(); // Get seller ID from Firestore
                    String profileImageUrl = document.getString("profileImageUrl");

                    if (profileImageUrl == null) {
                        profileImageUrl = "default_image_url_here"; // Replace with actual default image URL
                    }

                    shopList.add(new Shop(shopName, address, phone, sellerId, profileImageUrl));
                }
                shopAdapter.notifyDataSetChanged();
            }
        });
    }
}
