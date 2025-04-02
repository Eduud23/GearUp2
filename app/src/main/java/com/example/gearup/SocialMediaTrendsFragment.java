package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SocialMediaTrendsFragment extends Fragment {

    private FirebaseFirestore db;
    private BarChart searchQueryChart;
    private BarChart productMentionChart;
    private BarChart hashtagChart;

    private ArrayList<BarEntry> searchQueryEntries = new ArrayList<>();
    private ArrayList<BarEntry> productMentionEntries = new ArrayList<>();
    private ArrayList<BarEntry> hashtagEntries = new ArrayList<>();

    private Map<String, Integer> searchQueryCountMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_socialmedia_trends, container, false);

        // Access the Firestore instance of the fourth Firebase app
        db = FirebaseFirestore.getInstance(FirebaseApp.getInstance("gearupdataFourthApp"));

        // Initialize BarChart views
        searchQueryChart = view.findViewById(R.id.searchQueryChart);
        productMentionChart = view.findViewById(R.id.productMentionChart);
        hashtagChart = view.findViewById(R.id.hashtagChart);

        // Fetch data
        fetchData();

        return view;
    }

    private void fetchData() {
        // Fetch data from search_queries collection
        db.collection("search_queries")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int index = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String searchQuery = document.getString("search_query");

                            // Count occurrences of each search query
                            searchQueryCountMap.put(searchQuery, searchQueryCountMap.getOrDefault(searchQuery, 0) + 1);
                        }

                        // Populate BarChart with the counts of search queries
                        for (Map.Entry<String, Integer> entry : searchQueryCountMap.entrySet()) {
                            searchQueryEntries.add(new BarEntry(index, entry.getValue())); // Adding count as value
                            index++;
                        }

                        // Fetch data from product_mentions collection
                        fetchProductMentions();
                    } else {
                        Toast.makeText(getContext(), "Error fetching search queries", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchProductMentions() {
        db.collection("product_mentions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int index = searchQueryEntries.size();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String product = document.getString("product");
                            Object mentionsObj = document.get("mentions");

                            // Check if mentions is a number
                            long mentions = 0;
                            if (mentionsObj instanceof Long) {
                                mentions = (Long) mentionsObj; // Handle Long type
                            } else if (mentionsObj instanceof Integer) {
                                mentions = ((Integer) mentionsObj).longValue(); // Handle Integer type
                            } else if (mentionsObj instanceof String) {
                                try {
                                    mentions = Long.parseLong((String) mentionsObj); // Attempt to parse String to Long
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing mentions as Long: " + e.getMessage());
                                    continue; // Skip this document if parsing fails
                                }
                            }

                            productMentionEntries.add(new BarEntry(index, mentions)); // Example data
                            index++;
                        }

                        // Fetch data from hashtags collection
                        fetchHashtags();
                    } else {
                        Toast.makeText(getContext(), "Error fetching product mentions", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchHashtags() {
        db.collection("hashtags")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int index = productMentionEntries.size();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String hashtag = document.getString("hashtag");
                            hashtagEntries.add(new BarEntry(index, 1)); // Example data for hashtag
                            index++;
                        }

                        // Now that we have all data, populate the charts
                        populateCharts();
                    } else {
                        Toast.makeText(getContext(), "Error fetching hashtags", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateCharts() {
        // Create datasets and set them to the charts
        BarDataSet searchQueryDataSet = new BarDataSet(searchQueryEntries, "Search Queries");
        searchQueryDataSet.setColor(Color.BLUE);  // Example: Set color of the bars

        BarDataSet productMentionDataSet = new BarDataSet(productMentionEntries, "Product Mentions");
        productMentionDataSet.setColor(Color.GREEN);  // Example: Set color of the bars

        BarDataSet hashtagDataSet = new BarDataSet(hashtagEntries, "Hashtags");
        hashtagDataSet.setColor(Color.RED);  // Example: Set color of the bars

        // Create BarData for each chart
        BarData searchQueryData = new BarData(searchQueryDataSet);
        BarData productMentionData = new BarData(productMentionDataSet);
        BarData hashtagData = new BarData(hashtagDataSet);

        // Set data to the charts
        searchQueryChart.setData(searchQueryData);
        productMentionChart.setData(productMentionData);
        hashtagChart.setData(hashtagData);

        // Optional: Customize appearance
        searchQueryChart.invalidate(); // refresh chart
        productMentionChart.invalidate();
        hashtagChart.invalidate();

        // Optional: Adjust chart appearance (e.g., hide grid lines, customize labels)
        searchQueryChart.getDescription().setEnabled(false);
        productMentionChart.getDescription().setEnabled(false);
        hashtagChart.getDescription().setEnabled(false);
    }
}
