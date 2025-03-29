package com.example.gearup;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RecommendationManager {
    private static final String TAG = "RecommendationManager";
    private FirebaseFirestore db;

    public RecommendationManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void loadRecommendations(String currentUserId, Consumer<List<Product>> callback) {
        Log.d(TAG, "Current User ID: " + currentUserId);

        if (currentUserId != null) {
            CollaborativeFilteringRecommender.generateRecommendations(currentUserId, recommendedProductIds -> {
                Log.d(TAG, "Generated Recommendations: " + recommendedProductIds);

                if (recommendedProductIds == null || recommendedProductIds.isEmpty()) {
                    Log.d(TAG, "No recommendations found.");
                    callback.accept(new ArrayList<>());
                } else {
                    fetchProductDetails(recommendedProductIds, callback);
                }
            });
        } else {
            callback.accept(new ArrayList<>());
        }
    }

    private void fetchProductDetails(List<String> productIds, Consumer<List<Product>> callback) {
        Log.d(TAG, "Fetching details for product IDs: " + productIds);
        List<Product> recommendedProducts = new ArrayList<>();

        db.collectionGroup("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Product> tempProductList = new ArrayList<>();

                    for (QueryDocumentSnapshot productDoc : querySnapshot) {
                        String productId = productDoc.getId();

                        if (productIds.contains(productId)) {
                            String name = productDoc.getString("name");
                            Double priceValue = productDoc.getDouble("price");
                            double price = (priceValue != null) ? priceValue : 0.0;
                            String description = productDoc.getString("description");
                            String brand = productDoc.getString("brand");
                            String yearModel = productDoc.getString("yearModel");
                            List<String> imageUrls = (List<String>) productDoc.get("imageUrls");
                            String sellerId = productDoc.getString("sellerId");
                            Long quantityValue = productDoc.getLong("quantity");
                            int quantity = (quantityValue != null) ? quantityValue.intValue() : 0;

                            if (name != null && description != null && imageUrls != null && !imageUrls.isEmpty() && sellerId != null) {
                                Product product = new Product();
                                product.setId(productId);
                                product.setName(name);
                                product.setPrice(price);
                                product.setDescription(description);
                                product.setBrand(brand);
                                product.setYearModel(yearModel);
                                product.setImageUrls(imageUrls);
                                product.setSellerId(sellerId);
                                product.setQuantity(quantity);

                                tempProductList.add(product);
                            }
                        }
                    }

                    if (!tempProductList.isEmpty()) {
                        fetchSellerProfileImages(tempProductList, callback);
                    } else {
                        callback.accept(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching products", e);
                    callback.accept(new ArrayList<>());
                });
    }

    private void fetchSellerProfileImages(List<Product> products, Consumer<List<Product>> callback) {
        Log.d(TAG, "Fetching seller profile images for " + products.size() + " products");

        List<Product> updatedProducts = new ArrayList<>();
        int[] remainingRequests = {products.size()};

        for (Product product : products) {
            db.collection("sellers").document(product.getSellerId())
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String profileImageUrl = userDoc.getString("profileImageUrl");
                            product.setSellerProfileImageUrl(profileImageUrl != null ? profileImageUrl : "");
                        } else {
                            product.setSellerProfileImageUrl("");
                        }
                        synchronized (updatedProducts) {
                            updatedProducts.add(product);
                        }
                        if (--remainingRequests[0] == 0) {
                            callback.accept(updatedProducts);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching seller profile image", e);
                        product.setSellerProfileImageUrl("");
                        synchronized (updatedProducts) {
                            updatedProducts.add(product);
                        }
                        if (--remainingRequests[0] == 0) {
                            callback.accept(updatedProducts);
                        }
                    });
        }
    }
}
