package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SeeAllServicesActivity extends AppCompatActivity {

    private static final String TAG = "SeeAllServicesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_services);
        Log.d(TAG, "onCreate: SeeAllServicesActivity started");

        // Set up RecyclerView
        RecyclerView similarServicesRecycler = findViewById(R.id.allServicesRecycler);
        similarServicesRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        // Get the similar services from intent
        List<Object> similarServices = (List<Object>) getIntent().getSerializableExtra("similarServices");

        Log.d(TAG, "Total similar services received: " + (similarServices != null ? similarServices.size() : 0));

        // Set up the adapter with similar services
        if (similarServices != null) {
            RecommendCombinedAdapter adapter = new RecommendCombinedAdapter(this, similarServices);
            similarServicesRecycler.setAdapter(adapter);
        }
    }
}
