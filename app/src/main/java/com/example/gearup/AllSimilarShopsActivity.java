package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AllSimilarShopsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocalShopAdapter shopAdapter;
    private ArrayList<LocalShop> allShopsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_similar_shops);

        recyclerView = findViewById(R.id.recyclerViewAllShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get the similar shops passed via intent
        allShopsList = (ArrayList<LocalShop>) getIntent().getSerializableExtra("similarShops");

        // Set up the adapter with the shops list
        shopAdapter = new LocalShopAdapter(allShopsList, this, shop -> {
            Intent intent = new Intent(AllSimilarShopsActivity.this, LocalShopDetailsActivity.class);
            // Pass shop details to the next activity
            intent.putExtra("shopName", shop.getShopName());
            intent.putExtra("kindOfRepair", shop.getKindOfRepair());
            intent.putExtra("timeSchedule", shop.getTimeSchedule());
            intent.putExtra("place", shop.getPlace());
            intent.putExtra("ratings", shop.getRatings());
            intent.putExtra("image", shop.getImage());
            intent.putExtra("contactNumber", shop.getContactNumber());
            intent.putExtra("website", shop.getWebsite());
            intent.putExtra("latitude", shop.getLatitude());
            intent.putExtra("longitude", shop.getLongitude());
            startActivity(intent);
        });

        recyclerView.setAdapter(shopAdapter);
    }
}
