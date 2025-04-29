package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AllSimilarLocalProducts extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocalTrendsAdapter adapter;
    private List<LocalTrendsData> localProductsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_similar_local_products);

        // Setup toolbar with back navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSimilarProducts); // Make sure this ID matches your layout
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Get the product list from the Intent
        localProductsList = getIntent().getParcelableArrayListExtra("allProducts");

        if (localProductsList == null || localProductsList.isEmpty()) {
            Toast.makeText(this, "No local products found.", Toast.LENGTH_SHORT).show();
            localProductsList = new ArrayList<>();
        }

        // Setup Adapter
        adapter = new LocalTrendsAdapter(localProductsList, data -> {
            Intent intent = new Intent(AllSimilarLocalProducts.this, LocalTrendsDetails.class);
            intent.putExtra("image", data.getImage());
            intent.putExtra("name", data.getName());
            intent.putExtra("place", data.getPlace());
            intent.putExtra("price", data.getPrice());
            intent.putExtra("ratings", data.getRatings());
            intent.putExtra("sold", data.getSold());
            intent.putExtra("promo", data.getPromo());
            intent.putExtra("link", data.getLink());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}
