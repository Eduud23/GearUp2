package com.example.gearup;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart instance;
    private List<CartItem> items;
    private FirebaseFirestore db;
    private Context context;  // Context for Toast messages

    // Constructor accepts context
    private Cart(Context context) {
        this.context = context;
        items = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        loadCart();  // Load cart items from Firestore on initialization
    }

    // Singleton pattern to get Cart instance
    public static Cart getInstance(Context context) {
        if (instance == null) {
            instance = new Cart(context);  // Initialize with context
        }
        return instance;
    }

    // Add product to the cart
    public void addToCart(Product product, int quantity, String sellerId, String productId, String documentId) {
        // Extract product properties
        String productName = product.getName();  // Get product name
        double totalPrice = product.getPrice() * quantity;  // Total price calculation
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID
        String imageUrl = product.getImageUrls().isEmpty() ? "" : product.getImageUrls().get(0);  // Get image URL
        String brand = product.getBrand();  // Get brand
        String yearModel = product.getYearModel();  // Get year model

        // Create CartItem instance
        CartItem cartItem = new CartItem(productName, quantity, sellerId, totalPrice, userId, imageUrl, brand, yearModel, productId, documentId);

        // Add item to the cart
        items.add(cartItem);

        // Save updated cart to Firestore
        saveCart();
    }

    // Get items in the cart
    public List<CartItem> getItems() {
        return items;
    }

    // Save cart items to Firestore
    private void saveCart() {
        // Use batch write for efficiency (if there are multiple items to write)
        WriteBatch batch = db.batch();

        for (CartItem item : items) {
            DocumentReference docRef = db.collection("carts")
                    .document(item.getUserId())  // Use user ID as the document ID
                    .collection("items")
                    .document(item.getDocumentId());  // Use the document ID of the CartItem

            batch.set(docRef, item);  // Add to batch
        }

        // Commit the batch write
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    // Optionally, handle successful save
                    Toast.makeText(context, "Cart saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(context, "Failed to save cart", Toast.LENGTH_SHORT).show();
                });
    }

    // Load cart items from Firestore
    private void loadCart() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID

        db.collection("carts")
                .document(userId)  // Load cart for specific user
                .collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        items.clear();  // Clear current items
                        for (DocumentSnapshot document : task.getResult()) {
                            CartItem cartItem = document.toObject(CartItem.class);  // Convert document to CartItem
                            if (cartItem != null) {
                                items.add(cartItem);  // Add to local cart
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to load cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Remove item from the cart (both Firestore and local cart)
    public void removeFromCart(CartItem cartItem) {
        db.collection("carts")
                .document(cartItem.getUserId())  // Use user ID
                .collection("items")
                .document(cartItem.getDocumentId())  // Get the document ID of the item
                .delete()
                .addOnSuccessListener(aVoid -> {
                    items.remove(cartItem);  // Remove item from local cart after successful deletion
                    Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to remove item from cart", Toast.LENGTH_SHORT).show();
                });
    }
}
