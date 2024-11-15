package com.example.gearup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class OrderedProductsFragment extends Fragment {
    private RecyclerView recyclerViewOrdered;
    private PurchasedAdapter purchasedAdapter;
    private FirebaseFirestore db;
    private List<OrderItem> orderedItems;
    private ListenerRegistration orderedListenerRegistration;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordered_products, container, false);
        recyclerViewOrdered = view.findViewById(R.id.recyclerView_ordered);
        recyclerViewOrdered.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        orderedItems = new ArrayList<>();
        purchasedAdapter = new PurchasedAdapter(orderedItems);
        recyclerViewOrdered.setAdapter(purchasedAdapter);

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchOrderedItems();
        }

        return view;
    }

    private void fetchOrderedItems() {
        orderedListenerRegistration = db.collection("orders")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return; // Handle the error
                        }

                        orderedItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            OrderItem orderItem = doc.toObject(OrderItem.class);
                            orderedItems.add(orderItem);
                        }
                        purchasedAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (orderedListenerRegistration != null) {
            orderedListenerRegistration.remove();
        }
    }
}
