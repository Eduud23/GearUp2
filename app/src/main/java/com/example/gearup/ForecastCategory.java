package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseApp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ForecastCategory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ForecastCategoryAdapter adapter;
    private List<ForecastCategoryModel> forecastCategoryList;
    private FirebaseFirestore db;

    private String productTitle;
    private TextView productTitleTextView, forecastTotalTextView;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forecast_category);

        forecastTotalTextView = findViewById(R.id.forecastTotalTextView);
        productTitleTextView = findViewById(R.id.productTitleTextView);
        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());

        productTitle = getIntent().getStringExtra("productTitle");
        if (productTitle != null) {
            productTitleTextView.setText(productTitle);
        }

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
                        Map<String, List<Date>> categoryDates = new HashMap<>();
                        Map<String, List<Integer>> categoryQuantities = new HashMap<>();

                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String category = doc.getString("category");
                            Double quantity = doc.getDouble("quantity");
                            String dateString = doc.getString("date");

                            if (category != null && quantity != null && dateString != null) {
                                Date date = parseDate(dateString);
                                if (date != null && isValidForSelectedPeriod(date)) {
                                    categoryDates.putIfAbsent(category, new ArrayList<>());
                                    categoryQuantities.putIfAbsent(category, new ArrayList<>());

                                    categoryDates.get(category).add(date);
                                    categoryQuantities.get(category).add(quantity.intValue());
                                }
                            }
                        }

                        int totalPredictedQuantity = 0; // Variable to store the total predicted quantity

                        for (Map.Entry<String, List<Date>> entry : categoryDates.entrySet()) {
                            String category = entry.getKey();
                            List<Date> dates = entry.getValue();
                            List<Integer> quantities = categoryQuantities.get(category);

                            if (dates.size() >= 2 && quantities.size() >= 2) {
                                int predictedQuantity = performLinearRegression(dates, quantities);
                                forecastCategoryList.add(new ForecastCategoryModel(category, predictedQuantity));
                                totalPredictedQuantity += predictedQuantity; // Add the predicted quantity to the total
                            } else {
                                forecastCategoryList.add(new ForecastCategoryModel(category, 0));
                            }
                        }

                        // Display the total predicted forecast instead of the product title
                        forecastTotalTextView.setText("Predicted Quantity: " + totalPredictedQuantity + " units");

                        adapter = new ForecastCategoryAdapter(forecastCategoryList, productTitle);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching data from Firestore", e));
    }

    private Date parseDate(String dateString) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("ForecastCategory", "Error parsing date: " + dateString, e);
            return null;
        }
    }

    private boolean isValidForSelectedPeriod(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Check for the entire period (6 months for Rainy/Dry seasons)
        switch (productTitle) {
            case "Rainy Season":
                return isRainySeason(date); // Returns true for June-November
            case "Dry Season":
                return isDrySeason(date); // Returns true for December-May
            case "Next Month":
                return isNextMonth(date); // Check for the next month
            default:
                return false;
        }
    }

    private boolean isRainySeason(Date date) {
        int month = getMonth(date);
        return month >= Calendar.JUNE && month <= Calendar.NOVEMBER; // June to November
    }

    private boolean isDrySeason(Date date) {
        int month = getMonth(date);
        return month == Calendar.DECEMBER || month <= Calendar.MAY; // December to May
    }

    private boolean isNextMonth(Date date) {
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);
        int nextMonthValue = nextMonth.get(Calendar.MONTH);

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);
        return dateCal.get(Calendar.MONTH) == nextMonthValue;
    }

    private int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    private int performLinearRegression(List<Date> dates, List<Integer> quantities) {
        if (dates.size() < 2 || quantities.size() < 2) return 0;

        long startDateInMillis = dates.get(0).getTime();
        int n = dates.size();

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            long diffDays = (dates.get(i).getTime() - startDateInMillis) / (1000 * 60 * 60 * 24);
            int y = quantities.get(i);

            sumX += diffDays;
            sumY += y;
            sumXY += diffDays * y;
            sumX2 += diffDays * diffDays;
        }

        double denominator = (n * sumX2 - sumX * sumX);
        if (denominator == 0) return 0;

        double m = (n * sumXY - sumX * sumY) / denominator;
        double b = (sumY - m * sumX) / n;

        long nextDay = dates.get(n - 1).getTime() + (1000 * 60 * 60 * 24);
        long diffNextDay = (nextDay - startDateInMillis) / (1000 * 60 * 60 * 24);

        return (int) Math.round(m * diffNextDay + b);
    }
}
