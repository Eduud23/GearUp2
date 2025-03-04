package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class RecommendationActivity extends AppCompatActivity {
  /*  private RecyclerView recyclerView;
    private RecommendationAdapter recommendationAdapter;
    private static final String TAG = "RecommendationActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationAdapter = new RecommendationAdapter();
        recyclerView.setAdapter(recommendationAdapter);

        db = FirebaseFirestore.getInstance();

        loadRecommendations();
    }

    private void loadRecommendations() {
        String currentUserId = UserInteractionLogger.getCurrentUserId();
        Log.d(TAG, "Current User ID: " + currentUserId);

        if (currentUserId != null) {
            CollaborativeFilteringRecommender.generateRecommendations(currentUserId, recommendedProductIds -> {
                Log.d(TAG, "Generated Recommendations: " + recommendedProductIds);

                if (recommendedProductIds == null || recommendedProductIds.isEmpty()) {
                    Log.d(TAG, "No recommendations found.");
                } else {
                    fetchProductDetails(recommendedProductIds);
                }
            });
        }
    }

    private void fetchProductDetails(List<String> productIds) {
        Log.d(TAG, "Fetching details for product IDs: " + productIds);
        List<Product> recommendedProducts = new ArrayList<>();

        db.collectionGroup("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot productDoc : querySnapshot) {
                        String productId = productDoc.getId(); // Use document ID as product ID

                        if (productIds.contains(productId)) { // Check if this product is recommended
                            String name = productDoc.getString("name");
                            String category = productDoc.getString("category");
                            List<String> imageUrls = (List<String>) productDoc.get("imageUrls");

                            if (name != null && category != null && imageUrls != null && !imageUrls.isEmpty()) {
                                Product product = new Product();
                                product.setId(productId);
                                product.setName(name);
                                product.setCategory(category);
                                product.setImageUrls(imageUrls);

                                recommendedProducts.add(product);
                            }
                        }
                    }

                    // Update RecyclerView only if products were found
                    if (!recommendedProducts.isEmpty()) {
                        runOnUiThread(() -> recommendationAdapter.setProductList(recommendedProducts));
                    } else {
                        Log.d(TAG, "No matching products found in Firestore.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching products", e));
    }
*/
}
