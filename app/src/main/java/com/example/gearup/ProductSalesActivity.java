package com.example.gearup;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductSalesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerViewProducts;
    private SalesProductAdapter salesProductAdapter;
    private List<ProductSalesItem> productList;
    private String selectedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_sales);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the address passed from the previous activity
        selectedAddress = getIntent().getStringExtra("address");

        // Initialize RecyclerView and adapter
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        salesProductAdapter = new SalesProductAdapter(productList);
        recyclerViewProducts.setAdapter(salesProductAdapter);

        // Fetch top 10 products based on sales
        fetchTop10Products();
    }

    private void fetchTop10Products() {
        db.collection("sales")
                .whereEqualTo("address", selectedAddress) // Filter by address
                .orderBy("productQuantity", com.google.firebase.firestore.Query.Direction.DESCENDING) // Order by productQuantity
                .limit(10) // Get only top 10 products
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            productList.clear();
                            List<ProductSalesItem> tempList = new ArrayList<>();

                            // Loop through all the products
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String productName = document.getString("productName");
                                double productPrice = document.getDouble("productPrice");
                                int productQuantity = document.getLong("productQuantity").intValue();
                                String productYearModel = document.getString("productYearModel");
                                String productImage = document.getString("productImage");

                                // Create a ProductSalesItem object
                                ProductSalesItem item = new ProductSalesItem(productName, productPrice, productQuantity, productYearModel, productImage);

                                // Check if the product with the same name and year already exists in the tempList
                                boolean exists = false;
                                for (ProductSalesItem existingItem : tempList) {
                                    if (existingItem.getProductName().equals(productName) &&
                                            existingItem.getProductYearModel().equals(productYearModel)) {
                                        // If it exists, add the current product's quantity to the existing one
                                        existingItem.setProductQuantity(existingItem.getProductQuantity() + productQuantity);
                                        exists = true;
                                        break;
                                    }
                                }

                                // If the product doesn't exist, simply add it to the list
                                if (!exists) {
                                    tempList.add(item);
                                }
                            }

                            // Update the productList with the aggregated results
                            productList.addAll(tempList);

                            // Notify the adapter to refresh the RecyclerView
                            salesProductAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ProductSalesActivity.this, "No products found for this address", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ProductSalesActivity.this, "Error fetching products: " + task.getException(), Toast.LENGTH_LONG).show();
                    }
                });
    }




}
