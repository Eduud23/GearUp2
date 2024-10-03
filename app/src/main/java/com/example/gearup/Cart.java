package com.example.gearup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart instance;
    private List<CartItem> items;
    private FirebaseFirestore db;

    private Cart() {
        items = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        loadCart(); // Load cart items from Firestore when initialized
    }

    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    public void addToCart(Product product, int quantity) {
        items.add(new CartItem(product, quantity));
        saveCart(); // Save cart items to Firestore
    }

    public List<CartItem> getItems() {
        return items;
    }

    private void saveCart() {
        // Save each item in the cart to Firestore
        for (CartItem item : items) {
            // Assuming you have a structure for your cart in Firestore
            db.collection("carts").document("user_cart") // Use a unique user identifier
                    .collection("items")
                    .add(item); // Save each item
        }
    }

    private void loadCart() {
        db.collection("carts").document("user_cart")
                .collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        items.clear(); // Clear current items
                        for (DocumentSnapshot document : task.getResult()) {
                            CartItem cartItem = document.toObject(CartItem.class);
                            if (cartItem != null) {
                                items.add(cartItem); // Add to local cart
                            }
                        }
                    }
                });
    }
}