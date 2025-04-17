package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.Query;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ArrayList;

public class Forecast extends AppCompatActivity {

    private static final String TAG = "Forecasting";
    private TextView forecastTextView;
    private FirebaseFirestore db;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        // Initialize Firestore for the fifth Firebase app
        FirebaseApp gearupdataFifthApp = FirebaseApp.getInstance("gearupdataFifthApp");
        db = FirebaseFirestore.getInstance(gearupdataFifthApp);

        forecastTextView = findViewById(R.id.forecastTextView);
        barChart = findViewById(R.id.barChart);

        // Fetch the data and perform forecasting
        fetchDataAndForecast();
    }

    private void fetchDataAndForecast() {
        Log.d(TAG, "Fetching all sales data for debugging.");

        db.collection("sales_orders")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                        // Group data by product line
                        Map<String, List<DocumentSnapshot>> productLineMap = groupDataByProductLine(documents);

                        // Prepare a list for all BarEntries and product line names
                        List<BarEntry> barEntries = new ArrayList<>();
                        List<String> productLineNames = new ArrayList<>();
                        List<BarDataSet> barDataSets = new ArrayList<>();
                        int index = 0;  // This is used to set the x-axis position for each bar

                        // Iterate through each product line and perform linear regression
                        for (Map.Entry<String, List<DocumentSnapshot>> entry : productLineMap.entrySet()) {
                            String productLine = entry.getKey();
                            List<DocumentSnapshot> productData = entry.getValue();

                            // Arrays to store the independent (x) and dependent (y) variables
                            double[] x = new double[productData.size()]; // Dates (converted to numbers)
                            double[] y = new double[productData.size()]; // Total sales

                            int i = 0;
                            Date firstDate = null;

                            // Process the data for this product line
                            for (DocumentSnapshot document : productData) {
                                String dateString = document.getString("date");
                                Double total = document.getDouble("total");

                                if (dateString != null && total != null) {
                                    try {
                                        // Parse the date field
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                        Date date = dateFormat.parse(dateString);

                                        if (firstDate == null) {
                                            firstDate = date;
                                        }

                                        long daysSinceStart = (date.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);

                                        x[i] = daysSinceStart;
                                        y[i] = total;

                                        i++;
                                    } catch (Exception e) {
                                        Log.w(TAG, "Failed to parse date for document: " + document.getId(), e);
                                    }
                                }
                            }

                            if (i > 0) {
                                // Perform linear regression
                                double[] regressionResults = performLinearRegression(x, y);

                                // Forecast sales for the next month (30 days)
                                double forecastNextMonth = regressionResults[0] * (x.length + 30) + regressionResults[1];

                                // Display the forecasted sales for the next month in the TextView
                                forecastTextView.append("\n\n-----------------------------\n");
                                forecastTextView.append("Product Line: " + productLine + "\n");
                                forecastTextView.append("Forecast for the next month: " + String.format("%.2f", forecastNextMonth) + " units\n");

                                // Create a BarEntry for each product line (each forecast)
                                barEntries.add(new BarEntry(index, (float) forecastNextMonth));
                                productLineNames.add(productLine); // Add product line name to list

                                // Set a unique color for each product line (using index)
                                int color = getColorForProductLine(index);

                                // Create a new BarDataSet for each product line
                                BarDataSet barDataSet = new BarDataSet(barEntries, productLine);
                                barDataSet.setColor(color); // Set unique color for this product line
                                barDataSets.add(barDataSet);  // Add BarDataSet to the list

                                index++;  // Increment index to position bars for each product line

                                // Clear the barEntries list for the next product line
                                barEntries = new ArrayList<>();
                            }
                        }

                        // Cast the List<BarDataSet> to List<IBarDataSet>
                        List<IBarDataSet> iBarDataSets = new ArrayList<>(barDataSets);

                        // Create BarData using List<IBarDataSet>
                        BarData barData = new BarData(iBarDataSets); // Pass all BarDataSets
                        barChart.setData(barData);

                        // Disable x-axis labels
                        barChart.getXAxis().setDrawLabels(false);  // Hide all x-axis labels

                        // **Disable the legend (description labels)**
                        barChart.getLegend().setEnabled(false); // This line hides the legend (color description labels)

                        // Customize the bar chart appearance
                        barChart.invalidate();  // Refresh the chart

                    } else {
                        Log.w(TAG, "No data available for forecasting.");
                        forecastTextView.setText("No data available for forecasting.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching data", e);
                    forecastTextView.setText("Error fetching data.");
                });
    }



    // Method to get a unique color for each product line (you can customize this)
    private int getColorForProductLine(int index) {
        // You can define a color array to cycle through colors
        int[] colors = {
                0xFF1E88E5,  // Blue
                0xFF43A047,  // Green
                0xFFFF9800,  // Orange
                0xFFF44336,  // Red
                0xFF9C27B0,  // Purple
                0xFF2196F3   // Light Blue
        };
        return colors[index % colors.length];  // Cycle through colors if more than 6 product lines
    }



    // Group data by product line
    private Map<String, List<DocumentSnapshot>> groupDataByProductLine(List<DocumentSnapshot> documents) {
        Map<String, List<DocumentSnapshot>> productLineMap = new HashMap<>();

        for (DocumentSnapshot document : documents) {
            String productLine = document.getString("product_line");
            if (productLine != null) {
                productLineMap.computeIfAbsent(productLine, k -> new ArrayList<>()).add(document);
            }
        }

        return productLineMap;
    }

    // Perform Linear Regression
    private double[] performLinearRegression(double[] x, double[] y) {
        int N = x.length;

        // Calculate the sums needed for the slope (m) and intercept (b)
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < N; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
        }

        // Calculate the slope (m) and intercept (b)
        double m = (N * sumXY - sumX * sumY) / (N * sumX2 - sumX * sumX);
        double b = (sumY - m * sumX) / N;

        // Return the regression parameters
        return new double[]{m, b};
    }
}
