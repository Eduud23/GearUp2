package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ForecastDetail extends AppCompatActivity {

    private static final String TAG = "ForecastDetail";
    private RecyclerView recyclerView;
    private ForecastDetailAdapter adapter;
    private List<ForecastProductItem> productItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast_detail);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());

        String productTitle = getIntent().getStringExtra("productTitle");

        if (productTitle != null) {
            productTitle = normalizeProductTitle(productTitle);
        }
        TextView productTitleTextView = findViewById(R.id.productTitleText);
        productTitleTextView.setText(productTitle);


        recyclerView = findViewById(R.id.forecastDetailRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ForecastDetailAdapter(productItemList);
        recyclerView.setAdapter(adapter);

        FirebaseApp sixthApp = FirebaseApp.getInstance("gearupdataSixthApp");
        FirebaseFirestore db = FirebaseFirestore.getInstance(sixthApp);

        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build());

        CollectionReference productsRef = db.collection("products");
        productsRef.whereEqualTo("category", productTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productItemList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ForecastProductItem item = doc.toObject(ForecastProductItem.class);
                        productItemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error", e);
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                });
    }
    private String normalizeProductTitle(String title) {
        switch (title.toLowerCase()) {
            case "electrical system":
                return "Electrical System";
            case "suspension & traction":
                return "Suspension & Traction";
            case "frame & body":
                return "Frame & Body";
            case "braking system":
                return "Braking System";
            default:
                return title; // return as-is if no match
        }
    }

}
