package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SocialMediaTrendsFragment extends Fragment {

    private FirebaseFirestore db;
    private LineChart searchQueryChart;
    private LineChart productMentionChart;
    private LineChart hashtagChart;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Entry> searchQueryEntries = new ArrayList<>();
    private ArrayList<Entry> productMentionEntries = new ArrayList<>();
    private ArrayList<Entry> hashtagEntries = new ArrayList<>();

    private ArrayList<String> hashtagNames = new ArrayList<>();

    private Map<String, Integer> searchQueryCountMap = new HashMap<>();
    private ArrayList<String> productNames = new ArrayList<>(); // To store product names for product mentions

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_socialmedia_trends, container, false);

        // Access the Firestore instance of the fourth Firebase app
        db = FirebaseFirestore.getInstance(FirebaseApp.getInstance("gearupdataFourthApp"));

        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Initialize LineChart views
        searchQueryChart = view.findViewById(R.id.searchQueryChart);
        productMentionChart = view.findViewById(R.id.productMentionChart);
        hashtagChart = view.findViewById(R.id.hashtagChart);

        // Fetch data
        fetchData();

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reset the data
            searchQueryEntries.clear();
            productMentionEntries.clear();
            hashtagEntries.clear();

            searchQueryCountMap.clear();
            productNames.clear();
            hashtagNames.clear();

            // Refresh the data
            fetchData();
        });

        return view;
    }

    private void fetchData() {
        // Fetch data from search_queries collection
        db.collection("search_queries")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int index = 0;  // Define index here
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String searchQuery = document.getString("search_query");

                            // Initialize queryCount to 0
                            long queryCount = 0;
                            if (document.contains("query_count")) {
                                Object queryCountObj = document.get("query_count");

                                // Check if query_count is a valid number (Long, Integer, or String)
                                if (queryCountObj instanceof Long) {
                                    queryCount = (Long) queryCountObj;
                                } else if (queryCountObj instanceof Integer) {
                                    queryCount = ((Integer) queryCountObj).longValue();
                                } else if (queryCountObj instanceof String) {
                                    try {
                                        queryCount = Long.parseLong((String) queryCountObj);
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Error parsing query_count as Long: " + e.getMessage());
                                        queryCount = 0;  // Default to 0 if parsing fails
                                    }
                                }
                            }

                            // Add query count and search query to map
                            searchQueryCountMap.put(searchQuery, (int) queryCount);  // Use queryCount directly
                        }

                        // Sort and get top 10 search queries by query_count
                        ArrayList<Map.Entry<String, Integer>> sortedSearchQueryList = new ArrayList<>(searchQueryCountMap.entrySet());
                        sortedSearchQueryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())); // Sort in descending order

                        for (Map.Entry<String, Integer> entry : sortedSearchQueryList.subList(0, Math.min(10, sortedSearchQueryList.size()))) {
                            searchQueryEntries.add(new Entry(index, entry.getValue())); // Adding queryCount as value
                            index++;
                        }

                        // Fetch data from product_mentions collection (without passing index)
                        fetchProductMentions(); // Remove index argument
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
                        // Clear existing entries before adding new data
                        productMentionEntries.clear();
                        productNames.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String product = document.getString("product");
                            Object mentionsObj = document.get("mentions");

                            // Check if mentions is a valid number (Long, Integer, or String)
                            long mentions = 0;
                            if (mentionsObj instanceof Long) {
                                mentions = (Long) mentionsObj;
                            } else if (mentionsObj instanceof Integer) {
                                mentions = ((Integer) mentionsObj).longValue();
                            } else if (mentionsObj instanceof String) {
                                try {
                                    mentions = Long.parseLong((String) mentionsObj);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing mentions as Long: " + e.getMessage());
                                    continue; // Skip invalid data
                                }
                            }

                            // Add product data to lists
                            productMentionEntries.add(new Entry(productMentionEntries.size(), mentions)); // Sequential x-values
                            productNames.add(product); // Save product names in parallel with the entries
                        }

                        // Sort product mentions by count in descending order and limit to top 10
                        ArrayList<Entry> sortedProductMentionEntries = new ArrayList<>(productMentionEntries);
                        sortedProductMentionEntries.sort((entry1, entry2) -> Float.compare(entry2.getY(), entry1.getY())); // Sort by mentions

                        // Limit to top 10 product mentions
                        if (sortedProductMentionEntries.size() > 10) {
                            sortedProductMentionEntries = new ArrayList<>(sortedProductMentionEntries.subList(0, 10));
                        }

                        // Create a new list for top 10 product names
                        ArrayList<String> topProducts = new ArrayList<>();
                        for (int i = 0; i < sortedProductMentionEntries.size(); i++) {
                            topProducts.add(productNames.get(i)); // Add the corresponding product name
                        }

                        // Now update the product mention entries and names with the top 10
                        productMentionEntries.clear();
                        productNames.clear();
                        for (int i = 0; i < sortedProductMentionEntries.size(); i++) {
                            Entry entry = sortedProductMentionEntries.get(i);
                            // Set x-values to be sequential (i.e., 0, 1, 2, ..., 9) to prevent gaps
                            productMentionEntries.add(new Entry(i, entry.getY()));
                            productNames.add(topProducts.get(i)); // Add the corresponding product name
                        }
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
                        // Clear existing entries before adding new data
                        hashtagEntries.clear();
                        hashtagNames.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String hashtag = document.getString("hashtag");
                            Object hashtagCountObj = document.get("hashtag_count");

                            // Check if hashtag_count is a number (Long, Integer, or String)
                            long hashtagCount = 0;
                            if (hashtagCountObj instanceof Long) {
                                hashtagCount = (Long) hashtagCountObj;
                            } else if (hashtagCountObj instanceof Integer) {
                                hashtagCount = ((Integer) hashtagCountObj).longValue();
                            } else if (hashtagCountObj instanceof String) {
                                try {
                                    hashtagCount = Long.parseLong((String) hashtagCountObj);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing hashtag_count as Long: " + e.getMessage());
                                    continue; // Skip invalid data
                                }
                            }

                            // Add hashtag data to lists
                            hashtagEntries.add(new Entry(hashtagEntries.size(), hashtagCount));
                            hashtagNames.add(hashtag); // Save hashtag names in parallel with the entries
                        }

                        // Sort hashtags by count in descending order and limit to top 10
                        ArrayList<Entry> sortedHashtagEntries = new ArrayList<>(hashtagEntries);
                        sortedHashtagEntries.sort((entry1, entry2) -> Float.compare(entry2.getY(), entry1.getY())); // Sort by hashtag count

                        // Limit to top 10 hashtags
                        if (sortedHashtagEntries.size() > 10) {
                            sortedHashtagEntries = new ArrayList<>(sortedHashtagEntries.subList(0, 10));
                        }

                        // Create a new list for top 10 hashtag names
                        ArrayList<String> topHashtags = new ArrayList<>();
                        for (int i = 0; i < sortedHashtagEntries.size(); i++) {
                            topHashtags.add(hashtagNames.get(i)); // Add the corresponding hashtag name
                        }

                        // Now update the hashtag entries and names with the top 10
                        hashtagEntries.clear();
                        hashtagNames.clear();
                        for (int i = 0; i < sortedHashtagEntries.size(); i++) {
                            Entry entry = sortedHashtagEntries.get(i);
                            // Set x-values to be sequential (i.e., 0, 1, 2, ..., 9) to prevent gaps
                            hashtagEntries.add(new Entry(i, entry.getY()));
                            hashtagNames.add(topHashtags.get(i)); // Add the corresponding hashtag name
                        }

                        // Set up the LineChart with updated data
                        populateCharts();

                        // Stop the swipe refresh animation once data is fetched and charts are populated
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        Toast.makeText(getContext(), "Error fetching hashtags", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateCharts() {
        // Create LineDataSets and set them to the charts
        LineDataSet searchQueryDataSet = new LineDataSet(searchQueryEntries, "Search Queries");
        searchQueryDataSet.setColor(Color.BLUE);  // Example: Set color of the line
        searchQueryDataSet.setValueTextColor(Color.BLACK); // Optional: Set text color for values

        LineDataSet productMentionDataSet = new LineDataSet(productMentionEntries, "Product Mentions");
        productMentionDataSet.setColor(Color.GREEN);  // Example: Set color of the line
        productMentionDataSet.setValueTextColor(Color.BLACK);

        LineDataSet hashtagDataSet = new LineDataSet(hashtagEntries, "Hashtags");
        hashtagDataSet.setColor(Color.RED);  // Example: Set color of the line
        hashtagDataSet.setValueTextColor(Color.BLACK);

        // Create LineData for each chart
        LineData searchQueryData = new LineData(searchQueryDataSet);
        LineData productMentionData = new LineData(productMentionDataSet);
        LineData hashtagData = new LineData(hashtagDataSet);

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

        // Update the TextViews with percentages and data
        updateChartPercentages();
    }

    private void updateChartPercentages() {
        // Update Search Query Counts
        StringBuilder searchQueryText = new StringBuilder("Top 10 Search Queries:\n");
        // Sort the search queries based on the count in descending order
        ArrayList<Map.Entry<String, Integer>> sortedSearchQueryList = new ArrayList<>(searchQueryCountMap.entrySet());
        sortedSearchQueryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())); // Sort by count in descending order

        // Display top 10 search queries
        int counter = 1;
        for (Map.Entry<String, Integer> entry : sortedSearchQueryList.subList(0, Math.min(10, sortedSearchQueryList.size()))) {
            searchQueryText.append(counter)
                    .append(". ")
                    .append(entry.getKey())
                    .append("\n");
            counter++;
        }

        // Set the top 10 search queries in the TextView
        ((TextView) getView().findViewById(R.id.searchQueryPercentages)).setText(searchQueryText.toString());

        // Update Product Mention Counts
        StringBuilder productMentionText = new StringBuilder("Top 10 Product Mentions:\n");
        // Sort the product mentions by count in descending order
        ArrayList<Entry> sortedProductMentionEntries = new ArrayList<>(productMentionEntries);
        sortedProductMentionEntries.sort((entry1, entry2) -> Float.compare(entry2.getY(), entry1.getY())); // Sort by mentions in descending order

        // Display top 10 product mentions
        int productCounter = 1;
        for (int i = 0; i < Math.min(10, sortedProductMentionEntries.size()); i++) {
            Entry entry = sortedProductMentionEntries.get(i);
            productMentionText.append(productCounter)
                    .append(". ")
                    .append(productNames.get(i)) // Get the corresponding product name
                    .append("\n");
            productCounter++;
        }

        // Set the top 10 product mentions in the TextView
        ((TextView) getView().findViewById(R.id.productMentionPercentages)).setText(productMentionText.toString());

        // Update Hashtag Counts
        StringBuilder hashtagText = new StringBuilder("Top 10 Hashtags:\n");

        // Sort the hashtags by count in descending order
        ArrayList<Entry> sortedHashtagEntries = new ArrayList<>(hashtagEntries);
        sortedHashtagEntries.sort((entry1, entry2) -> Float.compare(entry2.getY(), entry1.getY())); // Sort by hashtag count in descending order

        // Display top 10 hashtags
        int hashtagCounter = 1;
        for (int i = 0; i < Math.min(10, sortedHashtagEntries.size()); i++) {
            Entry entry = sortedHashtagEntries.get(i);
            String hashtag = hashtagNames.get(i);  // Access the hashtag name
            hashtagText.append(hashtagCounter)
                    .append(". ")
                    .append(hashtag)
                    .append("\n");
            hashtagCounter++;
        }

        // Set the top 10 hashtags in the TextView
        ((TextView) getView().findViewById(R.id.hashtagPercentages)).setText(hashtagText.toString());
    }
}
