package com.example.gearup;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private static Cart instance;
    private List<CartItem> items;

    // Private constructor
    private Cart() {
        items = new ArrayList<>();
    }

    // Singleton instance getter
    public static Cart getInstance() {
        if (instance == null) {
            instance = new Cart();
        }
        return instance;
    }

    // Add product to cart
    public void addToCart(Product product, int quantity) {
        // Check if the product is already in the cart
        for (CartItem cartItem : items) {
            if (cartItem.getProduct().getId().equals(product.getId())) {
                // If it is, update the quantity
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                return;
            }
        }
        // If not, add a new item
        items.add(new CartItem(product, quantity));
    }

    // Get list of items in the cart
    public List<CartItem> getItems() {
        return items;
    }

    // Remove an item from the cart
    public void removeFromCart(Product product) {
        items.removeIf(cartItem -> cartItem.getProduct().getId().equals(product.getId()));
    }

    // Clear the cart
    public void clearCart() {
        items.clear();
    }

    // Get total item count
    public int getTotalItemCount() {
        int totalCount = 0;
        for (CartItem cartItem : items) {
            totalCount += cartItem.getQuantity();
        }
        return totalCount;
    }

    // Get total price of items in the cart
    public double getTotalPrice() {
        double totalPrice = 0.0;
        for (CartItem cartItem : items) {
            totalPrice += cartItem.getProduct().getPrice() * cartItem.getQuantity();
        }
        return totalPrice;
    }
}
