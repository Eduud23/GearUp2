package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
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
    }

    private void fetchDataAndForecast() {
        db.collection("sales_orders")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        Map<String, List<DocumentSnapshot>> grouped = groupDataByProductLine(documents);
                        List<ForecastModel> chartDataList = new ArrayList<>();

                        for (Map.Entry<String, List<DocumentSnapshot>> entry : grouped.entrySet()) {
                            String productLine = entry.getKey();
                            List<DocumentSnapshot> data = entry.getValue();

                            List<Float> x = new ArrayList<>();
                            List<Float> y = new ArrayList<>();
                            List<String> labels = new ArrayList<>();

                            Date firstDate = null;

                            try {
                                for (DocumentSnapshot doc : data) {
                                    String dateString = doc.getString("date");
                                    Double total = doc.getDouble("total_php");

                                    if (dateString != null && total != null) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        Date date = sdf.parse(dateString);

                                        if (firstDate == null) firstDate = date;
                                        long days = (date.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);

                                        x.add((float) days);
                                        y.add(total.floatValue());
                                        labels.add(dateString);
                                    }
                                }

                                if (x.size() > 1) {
                                    double[] regression = performLinearRegression(x, y);

                                    // Forecast from current date
                                    Date currentDate = new Date();
                                    long daysFromFirstToNow = (currentDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24);
                                    float todayX = (float) daysFromFirstToNow;

                                    float forecastX = todayX + 30;
                                    float forecastY = (float) (regression[0] * forecastX + regression[1]);

                                    x.add(forecastX);
                                    y.add(forecastY);
                                    labels.add("Forecast");

                                    // Forecast date = today + 30 days
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(currentDate);
                                    cal.add(Calendar.DAY_OF_YEAR, 30);
                                    String forecastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

                                    // Determine trend direction
                                    String trendDirection;
                                    if (regression[0] > 0) {
                                        trendDirection = "Increasing";
                                    } else if (regression[0] < 0) {
                                        trendDirection = "Decreasing";
                                    } else {
                                        trendDirection = "Flat";
                                    }

                                    chartDataList.add(new ForecastModel(
                                            productLine,
                                            x, y, labels,
                                            forecastDate,
                                            forecastY,
                                            trendDirection
                                    ));
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Data parsing error", e);
                            }
                        }

                        chartAdapter = new LineChartAdapter(chartDataList);
                        recyclerView.setAdapter(chartAdapter);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore fetch failed", e));
    }

    private Map<String, List<DocumentSnapshot>> groupDataByProductLine(List<DocumentSnapshot> documents) {
        Map<String, List<DocumentSnapshot>> map = new HashMap<>();
        for (DocumentSnapshot doc : documents) {
            String productLine = doc.getString("product_line");
            if (productLine != null) {
                map.computeIfAbsent(productLine, k -> new ArrayList<>()).add(doc);
            }
        }
        return map;
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

        return new double[]{m, b}; // m = slope, b = intercept
    }
}
