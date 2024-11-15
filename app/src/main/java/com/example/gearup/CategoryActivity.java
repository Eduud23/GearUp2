package com.example.gearup;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private Map<String, List<Product>> categorizedProducts;
    private Map<String, Integer> categoryImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        recyclerView = findViewById(R.id.recyclerView_category);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InventoryAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        InventoryManager inventoryManager = InventoryManager.getInstance();
        categorizedProducts = inventoryManager.getCategorizedProducts();
        categoryImages = inventoryManager.getCategoryImages();

        String category = getIntent().getStringExtra("CATEGORY");

        if (category != null) {
            loadProductsByCategory(category);
        } else {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductsByCategory(String category) {
        List<Product> products = categorizedProducts.get(category);
        if (products != null) {
            productList.clear();
            productList.addAll(products);
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No products found for this category", Toast.LENGTH_SHORT).show();
        }
    }
}
