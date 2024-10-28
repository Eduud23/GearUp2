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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private RecyclerView recyclerViewPurchased;
    private RecyclerView recyclerViewCart;
    private PurchasedAdapter purchasedAdapter;
    private CartAdapter cartAdapter;
    private FirebaseFirestore db;
    private List<OrderItem> purchasedItems;
    private List<CartItem> cartItems; // List for cart items
    private ListenerRegistration purchasedListenerRegistration;
    private ListenerRegistration cartListenerRegistration; // Listener for cart items

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recyclerViewPurchased = view.findViewById(R.id.recyclerView_purchased);
        recyclerViewCart = view.findViewById(R.id.recyclerView_cart);

        recyclerViewPurchased.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        purchasedItems = new ArrayList<>();
        cartItems = new ArrayList<>();

        purchasedAdapter = new PurchasedAdapter(purchasedItems);
        cartAdapter = new CartAdapter(cartItems);

        recyclerViewPurchased.setAdapter(purchasedAdapter);
        recyclerViewCart.setAdapter(cartAdapter);

        fetchPurchasedItems();
        fetchCartItems(); // Fetch cart items

        return view;
    }

    private void fetchPurchasedItems() {
        purchasedListenerRegistration = db.collection("orders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return; // Handle the error
                        }

                        purchasedItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            OrderItem orderItem = doc.toObject(OrderItem.class);
                            purchasedItems.add(orderItem);
                        }
                        purchasedAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    }
                });
    }

    private void fetchCartItems() {
        cartListenerRegistration = db.collection("carts") // Assuming your cart items are stored in a "cart" collection
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return; // Handle the error
                        }

                        cartItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem cartItem = doc.toObject(CartItem.class);
                            cartItems.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (purchasedListenerRegistration != null) {
            purchasedListenerRegistration.remove(); // Unregister the listener to avoid memory leaks
        }
        if (cartListenerRegistration != null) {
            cartListenerRegistration.remove(); // Unregister cart listener
        }
    }
}
