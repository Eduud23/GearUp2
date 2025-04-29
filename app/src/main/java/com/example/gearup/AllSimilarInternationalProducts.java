package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AllSimilarInternationalProducts extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PopularProductAdapter adapter;
    private List<PopularProduct> allProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_similar_international_products);

        // Get the list of products passed from the previous activity
        allProducts = getIntent().getParcelableArrayListExtra("allProducts");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSimilarProducts);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize the adapter and set it to the RecyclerView
        if (allProducts != null && !allProducts.isEmpty()) {
            adapter = new PopularProductAdapter(allProducts, product -> {
                // Handle click on a product (navigate to its details page)
                Intent intent = new Intent(AllSimilarInternationalProducts.this, PopularProductDetail.class);
                intent.putExtra("title", product.getTitle());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("imageUrl", product.getImageUrl());
                intent.putExtra("itemUrl", product.getItemUrl());
                intent.putExtra("condition", product.getCondition());
                intent.putExtra("location", product.getLocation());
                intent.putExtra("shippingCost", product.getShippingCost());
                intent.putExtra("seller", product.getSeller());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        } else {
            // Handle the case when no products are passed
            Toast.makeText(this, "No similar products found.", Toast.LENGTH_SHORT).show();
        }
    }
}
