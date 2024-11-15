package com.example.gearup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryManager {
    private static InventoryManager instance;
    private Map<String, List<Product>> categorizedProducts = new HashMap<>();
    private Map<String, Integer> categoryImages = new HashMap<>();

    private InventoryManager() {}

    public static InventoryManager getInstance() {
        if (instance == null) {
            instance = new InventoryManager();
        }
        return instance;
    }

    public Map<String, List<Product>> getCategorizedProducts() {
        return categorizedProducts;
    }

    public Map<String, Integer> getCategoryImages() {
        return categoryImages;
    }

    public void addProduct(String category, Product product) {
        if (!categorizedProducts.containsKey(category)) {
            categorizedProducts.put(category, new ArrayList<>());
        }
        categorizedProducts.get(category).add(product);
    }

    public void addCategoryImage(String category, int imageResource) {
        categoryImages.put(category, imageResource);
    }
}
