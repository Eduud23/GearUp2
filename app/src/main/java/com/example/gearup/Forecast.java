package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.*;

public class Forecast extends AppCompatActivity {

    private static final String TAG = "Forecasting";
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private LineChartAdapter chartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        FirebaseApp gearupdataFifthApp = FirebaseApp.getInstance("gearupdataFifthApp");
        db = FirebaseFirestore.getInstance(gearupdataFifthApp);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchDataAndForecast();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void fetchDataAndForecast() {
        db.collection("sales_orders")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        List<ForecastModel> chartDataList = new ArrayList<>();

                        // Separate seasonal data
                        List<DocumentSnapshot> drySeasonDocs = new ArrayList<>();
                        List<DocumentSnapshot> rainySeasonDocs = new ArrayList<>();

                        // Group documents into dry or rainy season based on the date
                        for (DocumentSnapshot doc : documents) {
                            String dateString = doc.getString("date");
                            if (dateString != null) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    Date date = sdf.parse(dateString);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    int month = cal.get(Calendar.MONTH) + 1; // Months are 0-based

                                    // Log the date and the month for debugging
                                    Log.d(TAG, "Document Date: " + dateString + " Month: " + month);

                                    if (month >= 12 || month <= 5) {
                                        drySeasonDocs.add(doc);
                                        Log.d(TAG, "Added to Dry Season: " + dateString);
                                    } else {
                                        rainySeasonDocs.add(doc);
                                        Log.d(TAG, "Added to Rainy Season: " + dateString);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Season filtering error", e);
                                }
                            }
                        }

                        // Log the number of documents in both seasons
                        Log.d(TAG, "Dry Season Documents: " + drySeasonDocs.size());
                        Log.d(TAG, "Rainy Season Documents: " + rainySeasonDocs.size());

                        // Add seasonal forecasts first (dry and rainy seasons)
                        ForecastModel rainyForecast = generateSeasonalForecast("Rainy Season", rainySeasonDocs);
                        if (rainyForecast != null) chartDataList.add(rainyForecast);

                        ForecastModel dryForecast = generateSeasonalForecast("Dry Season", drySeasonDocs);
                        if (dryForecast != null) chartDataList.add(dryForecast);

                        // Generate forecast for the entire dataset (not specific product lines)
                        List<Float> x = new ArrayList<>();
                        List<Float> y = new ArrayList<>();
                        List<Float> quantity = new ArrayList<>();
                        List<String> labels = new ArrayList<>();

                        Date firstDate = null;

                        // Using entire dataset (not grouped by category)
                        for (DocumentSnapshot doc : documents) {
                            String dateString = doc.getString("date");
                            Double total = doc.getDouble("total_php");
                            Double qty = doc.getDouble("quantity");

                            if (dateString != null && total != null && qty != null) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    Date date = sdf.parse(dateString);

                                    if (firstDate == null) firstDate = date;
                                    long days = (date.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);

                                    x.add((float) days);
                                    y.add(total.floatValue());
                                    quantity.add(qty.floatValue());
                                    labels.add(dateString);
                                } catch (Exception e) {
                                    Log.e(TAG, "Data parsing error", e);
                                }
                            }
                        }

                        // Proceed with forecasting for the entire dataset
                        if (x.size() > 1) {
                            double[] regressionSales = performLinearRegression(x, y);
                            double[] regressionQuantity = performLinearRegression(x, quantity);

                            Date currentDate = new Date();
                            long daysFromFirstToNow = (currentDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);
                            float todayX = (float) daysFromFirstToNow;

                            float forecastX = todayX + 30; // Predict 30 days ahead

                            float forecastSales = (float) (regressionSales[0] * forecastX + regressionSales[1]);
                            float forecastQuantity = (float) (regressionQuantity[0] * forecastX + regressionQuantity[1]);

                            x.add(forecastX);
                            y.add(forecastSales);
                            quantity.add(forecastQuantity);
                            labels.add("Forecast");

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(currentDate);
                            cal.add(Calendar.MONTH, 1);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            String forecastStartDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
                            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                            String forecastEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

                            String forecastDate = forecastStartDate + " to " + forecastEndDate;

                            float lastActualSales = y.get(y.size() - 2);
                            String trendDirection;
                            if (forecastSales > lastActualSales) {
                                trendDirection = "Increasing";
                            } else if (forecastSales < lastActualSales) {
                                trendDirection = "Decreasing";
                            } else {
                                trendDirection = "Flat";
                            }

                            // Add the general forecast (for the whole dataset)
                            chartDataList.add(new ForecastModel(
                                    "Next Month",
                                    x, y, labels,
                                    forecastDate,
                                    forecastSales,
                                    forecastQuantity,
                                    trendDirection
                            ));
                        }

                        // Set the adapter with the final chart data
                        chartAdapter = new LineChartAdapter(chartDataList);
                        recyclerView.setAdapter(chartAdapter);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed", e));
    }

    private ForecastModel generateSeasonalForecast(String seasonName, List<DocumentSnapshot> docs) {
        if (docs.isEmpty()) return null;

        List<Float> x = new ArrayList<>();
        List<Float> y = new ArrayList<>();
        List<Float> quantity = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Date firstDate = null;

        try {
            for (DocumentSnapshot doc : docs) {
                String dateString = doc.getString("date");
                Double total = doc.getDouble("total_php");
                Double qty = doc.getDouble("quantity");

                if (dateString != null && total != null && qty != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = sdf.parse(dateString);

                    // NO NEED TO CALCULATE DAYS
                    // long days = (date.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);
                    x.add((float) x.size()); // <-- just use the index

                    y.add(total.floatValue());
                    quantity.add(qty.floatValue());
                    labels.add(dateString);
                }
            }


            if (x.size() > 1) {
                double[] regressionSales = performLinearRegression(x, y);
                double[] regressionQuantity = performLinearRegression(x, quantity);

                // Predict the forecast for the season
                float forecastX = x.get(x.size() - 1) + 30; // Predict 30 days ahead for the season

                // Base prediction (for 30 days ahead)
                float forecastSales = (float) (regressionSales[0] * forecastX + regressionSales[1]);
                float forecastQuantity = (float) (regressionQuantity[0] * forecastX + regressionQuantity[1]);

                // Scale the forecast to account for 6 months (rainy or dry season)
                // We will multiply the forecast by a factor, e.g., scaling factor of 6 months
                if (seasonName.equalsIgnoreCase("Dry Season") || seasonName.equalsIgnoreCase("Rainy Season")) {
                    forecastSales *= 1.5; // Adjust this factor as per your data; 1.5 means 50% higher forecast for 6 months
                    forecastQuantity *= 1.5;
                }

                x.add(forecastX);
                y.add(forecastSales);
                quantity.add(forecastQuantity);
                labels.add("Forecast");

                // Set the forecasted date range (6 months span for Dry and Rainy Season)
                String forecastDate = seasonName.equalsIgnoreCase("Dry Season") ?
                        "December to May" : "June to November";

                // Determine trend (Increasing, Decreasing, Flat)
                float lastActualSales = y.get(y.size() - 2);
                String trend;
                if (forecastSales > lastActualSales) {
                    trend = "Increasing";
                } else if (forecastSales < lastActualSales) {
                    trend = "Decreasing";
                } else {
                    trend = "Flat";
                }

                // Return the forecast model with adjusted sales prediction
                return new ForecastModel(seasonName, x, y, labels, forecastDate, forecastSales, forecastQuantity, trend);
            }
        } catch (Exception e) {
            Log.e(TAG, "Seasonal forecast error", e);
        }

        return null;
    }

    private double[] performLinearRegression(List<Float> x, List<Float> y) {
        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
        }

        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double b = (sumY - m * sumX) / n;

        return new double[]{m, b};
    }
}
