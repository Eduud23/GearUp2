package com.example.gearup;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart instance;
    private List<CartItem> items;
    private FirebaseFirestore db;
    private Context context;  // Add context to the Cart class

    // Constructor to accept the context
    private Cart(Context context) {
        this.context = context;  // Set the context
        items = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        loadCart(); // Load cart items from Firestore when initialized
    }

    // Singleton pattern to get the Cart instance
    public static Cart getInstance(Context context) {
        if (instance == null) {
            instance = new Cart(context);  // Pass context when creating the Cart instance
        }
        return instance;
    }

    // Add a product to the cart
    public void addToCart(Product product, int quantity, String sellerId, String productId) {
        // Extract product properties to pass to the CartItem constructor
        String productName = product.getName();  // Assuming Product class has getName() method
        double totalPrice = product.getPrice() * quantity;  // Assuming Product has getPrice() method
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Assuming you're getting the user ID from Firebase
        String imageUrl = product.getImageUrls().isEmpty() ? "" : product.getImageUrls().get(0);
        // Assuming Product has getImageUrls() method
        String brand = product.getBrand();  // Assuming Product has getBrand() method
        String yearModel = product.getYearModel();  // Assuming Product has getYearModel() method

        // Create the CartItem with the productId
        CartItem cartItem = new CartItem(productName, quantity, sellerId, totalPrice, userId, imageUrl, brand, yearModel, productId);

        // Add to the list of items in the cart
        items.add(cartItem);

        // Save cart items to Firestore
        saveCart();
    }

    // Get all items in the cart
    public List<CartItem> getItems() {
        return items;
    }

    // Save cart items to Firestore
    private void saveCart() {
        // Save each item in the cart to Firestore
        for (CartItem item : items) {
            // Using the userId to save under the correct user
            db.collection("carts")
                    .document(item.getUserId()) // Using userId as document identifier
                    .collection("items")
                    .add(item) // Save each item in Firestore
                    .addOnSuccessListener(documentReference -> {
                        // Optionally handle success
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(context, "Failed to save cart", Toast.LENGTH_SHORT).show();  // Use context to show the toast
                    });
        }
    }

    // Load cart items from Firestore
    private void loadCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID

        db.collection("carts")
                .document(userId) // Load cart for the specific user
                .collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        items.clear(); // Clear current cart items
                        for (DocumentSnapshot document : task.getResult()) {
                            // Convert document to CartItem object
                            CartItem cartItem = document.toObject(CartItem.class);
                            if (cartItem != null) {
                                // Add to the local cart
                                items.add(cartItem);
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to load cart", Toast.LENGTH_SHORT).show();  // Use context to show the toast
                    }
                });
    }

    // Remove a cart item from Firestore and local cart
    public void removeFromCart(CartItem cartItem) {
        // Remove from Firestore
        db.collection("carts")
                .document(cartItem.getUserId()) // Using userId as the document identifier
                .collection("items")
                .document(cartItem.getDocumentId()) // Get the Firestore document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local cart after successful deletion from Firestore
                    items.remove(cartItem);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(context, "Failed to remove item from cart", Toast.LENGTH_SHORT).show();  // Use context to show the toast
                });
    }
}
