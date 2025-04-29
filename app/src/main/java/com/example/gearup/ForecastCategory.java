package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForecastCategory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ForecastCategoryAdapter adapter;
    private List<ForecastCategoryModel> forecastCategoryList;
    private FirebaseFirestore db;

    private TextView productTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forecast_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        FirebaseApp gearupdataFifthApp = FirebaseApp.getInstance("gearupdataFifthApp");
        db = FirebaseFirestore.getInstance(gearupdataFifthApp);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        forecastCategoryList = new ArrayList<>();

        fetchCategoryDataFromFirestore();
    }

    private void fetchCategoryDataFromFirestore() {
        CollectionReference salesOrdersCollection = db.collection("sales_orders");

        salesOrdersCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Set<String> categorySet = new HashSet<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String category = doc.getString("category");

                            // Only add the category if it's not null
                            if (category != null) {
                                categorySet.add(category);
                            }
                        }

                        // Convert the Set to a list and create ForecastCategoryModel for each category
                        for (String category : categorySet) {
                            forecastCategoryList.add(new ForecastCategoryModel(category, 0));
                        }

                        // Initialize the adapter with the list of categories
                        adapter = new ForecastCategoryAdapter(forecastCategoryList);

                        // Set the adapter to the RecyclerView
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching data from Firestore", e));
    }
}
