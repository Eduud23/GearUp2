package com.example.gearup;

import java.util.ArrayList;
import java.util.List;

public class PurchasedOrder {
    private static PurchasedOrder instance; // Singleton instance
    private List<OrderItem> purchasedItems; // List to hold purchased items

    // Private constructor to prevent instantiation
    private PurchasedOrder() {
        purchasedItems = new ArrayList<>();
    }

    // Public method to get the singleton instance
    public static PurchasedOrder getInstance() {
        if (instance == null) {
            instance = new PurchasedOrder(); // Create the instance if it doesn't exist
        }
        return instance; // Return the existing instance
    }

    // Method to get the list of purchased items
    public List<OrderItem> getItems() {
        return purchasedItems;
    }

    // Method to add a purchased item
    public void addItem(OrderItem item) {
        purchasedItems.add(item);
    }
}
