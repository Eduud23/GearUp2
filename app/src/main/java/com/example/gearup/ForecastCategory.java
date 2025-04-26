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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForecastCategory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ForecastCategoryAdapter adapter;
    private List<ForecastCategoryModel> forecastCategoryList;
    private FirebaseFirestore db;

    private int forecastedQuantity;
    private String productTitle;
    private TextView productTitleTextView, forecastTotalTextView;

    // Date format for parsing the date in "yyyy-MM-dd" format
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forecast_category);
        forecastTotalTextView = findViewById(R.id.forecastTotalTextView);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());


        // Initialize the TextView
        productTitleTextView = findViewById(R.id.productTitleTextView);

        // Get the aggregated forecasted quantity and productTitle passed from the intent
        forecastedQuantity = getIntent().getIntExtra("forecastedQuantity", 0);
        productTitle = getIntent().getStringExtra("productTitle");

        // Set the product title in the TextView
        if (productTitle != null) {
            productTitleTextView.setText(productTitle);
        }

        FirebaseApp gearupdataFifthApp = FirebaseApp.getInstance("gearupdataFifthApp");
        db = FirebaseFirestore.getInstance(gearupdataFifthApp);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        forecastCategoryList = new ArrayList<>();

        // Fetch and process data based on title
        fetchCategoryDataFromFirestore();
    }

    private void fetchCategoryDataFromFirestore() {
        CollectionReference salesOrdersCollection = db.collection("sales_orders");

        salesOrdersCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                        // Map to hold sales data by category
                        Map<String, List<Date>> categoryDates = new HashMap<>();
                        Map<String, List<Integer>> categoryQuantities = new HashMap<>();

                        // Loop through documents and organize sales data by category
                        for (DocumentSnapshot doc : documents) {
                            String category = doc.getString("category");
                            Double quantity = doc.getDouble("quantity");
                            String dateString = doc.getString("date");  // Date as String (yyyy-MM-dd)

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

                        // Now, determine the forecasting logic based on the title and category
                        for (Map.Entry<String, List<Date>> entry : categoryDates.entrySet()) {
                            String category = entry.getKey();
                            List<Date> dates = entry.getValue();
                            List<Integer> quantities = categoryQuantities.get(category);

                            // Perform linear regression for each category separately
                            int predictedQuantity = performLinearRegression(dates, quantities);
                            Log.d("ForecastCategory", "Predicted Quantity for " + category + ": " + predictedQuantity);

                            // Add the predicted quantity for each category to the forecastCategoryList
                            forecastCategoryList.add(new ForecastCategoryModel(category + " Forecast", predictedQuantity));
                        }

                        // Call the appropriate forecasting method based on the productTitle
                        if (productTitle != null) {
                            switch (productTitle) {
                                case "Rainy Season":
                                    forecastForRainySeason(categoryQuantities);  // Adjust for rainy season
                                    break;
                                case "Dry Season":
                                    forecastForDrySeason(categoryQuantities);    // Adjust for dry season
                                    break;
                                case "Next Month":
                                    forecastForNextMonth(categoryQuantities);    // Adjust for next month
                                    break;
                                default:
                                    Log.e("ForecastCategory", "Invalid product title");
                                    break;
                            }
                        }

                        // Set the adapter with the updated forecast data
                        adapter = new ForecastCategoryAdapter(forecastCategoryList, productTitle);
                        recyclerView.setAdapter(adapter);

                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching data from Firestore", e));
    }

    // Parse the date from the "yyyy-MM-dd" format
    private Date parseDate(String dateString) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("ForecastCategory", "Error parsing date: " + dateString, e);
            return null;
        }
    }

    // Check if the date is valid for the selected period (based on productTitle)
    private boolean isValidForSelectedPeriod(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (productTitle) {
            case "Rainy Season":
                return isRainySeason(date);
            case "Dry Season":
                return isDrySeason(date);
            case "Next Month":
                return isNextMonth(date);
            default:
                return false;  // Invalid title or period
        }
    }

    // Linear regression to predict the next value
    private int performLinearRegression(List<Date> dates, List<Integer> quantities) {
        // Convert the dates to a numeric format (days since the first date)
        long startDateInMillis = dates.get(0).getTime();
        int n = dates.size();

        // Calculate the sums needed for the linear regression formula
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            long diffDays = (dates.get(i).getTime() - startDateInMillis) / (1000 * 60 * 60 * 24); // Days since first date
            sumX += diffDays;
            sumY += quantities.get(i);
            sumXY += diffDays * quantities.get(i);
            sumX2 += diffDays * diffDays;
        }

        // Calculate the slope (m) and intercept (b) for the linear regression line
        double m = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double b = (sumY - m * sumX) / n;

        // Predict the value for the next day (after the last date)
        long nextDay = dates.get(n - 1).getTime() + (1000 * 60 * 60 * 24); // Next day after the last date
        long diffNextDay = (nextDay - startDateInMillis) / (1000 * 60 * 60 * 24);

        // Calculate the predicted quantity for the next day
        double predictedQuantity = m * diffNextDay + b;

        return (int) Math.round(predictedQuantity); // Round to the nearest integer
    }

    // Validate if the date falls within the Rainy Season (June to November)
    private boolean isRainySeason(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH); // 0 = January, 1 = February, ..., 5 = June, ..., 10 = November
        return (month >= Calendar.JUNE && month <= Calendar.NOVEMBER);
    }

    // Validate if the date falls within the Dry Season (December to May)
    private boolean isDrySeason(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        return (month == Calendar.DECEMBER || month == Calendar.JANUARY || month == Calendar.FEBRUARY ||
                month == Calendar.MARCH || month == Calendar.APRIL || month == Calendar.MAY);
    }

    // Validate if the date falls within the Next Month
    private boolean isNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);  // Move to the next month
        int nextMonthValue = nextMonth.get(Calendar.MONTH);

        return calendar.get(Calendar.MONTH) == nextMonthValue;
    }

    // Function to handle the Rainy Season forecast (June to November)
    private void forecastForRainySeason(Map<String, List<Integer>> categoryQuantities) {
        forecastCategoryList.clear();

        // Total sum across all categories
        int totalHistorical = categoryQuantities.values().stream()
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .sum();

        int totalForecast = forecastedQuantity * 6;

        for (Map.Entry<String, List<Integer>> entry : categoryQuantities.entrySet()) {
            int categorySum = entry.getValue().stream().mapToInt(Integer::intValue).sum();
            int adjustedQuantity = totalHistorical == 0 ? 0 : (int) Math.round((double) categorySum / totalHistorical * totalForecast);
            forecastCategoryList.add(new ForecastCategoryModel(entry.getKey(), adjustedQuantity));
        }

        // ✅ Set the total forecast in the TextView instead of the list
        forecastTotalTextView.setText("Total Forecast: " + totalForecast + " units");
    }


    // Function to handle the Dry Season forecast (December to May)
    private void forecastForDrySeason(Map<String, List<Integer>> categoryQuantities) {
        forecastCategoryList.clear();

        int totalHistorical = categoryQuantities.values().stream()
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .sum();

        int totalForecast = forecastedQuantity * 6;

        for (Map.Entry<String, List<Integer>> entry : categoryQuantities.entrySet()) {
            int categorySum = entry.getValue().stream().mapToInt(Integer::intValue).sum();
            int adjustedQuantity = totalHistorical == 0 ? 0 :
                    (int) Math.round((double) categorySum / totalHistorical * totalForecast);
            forecastCategoryList.add(new ForecastCategoryModel(entry.getKey(), adjustedQuantity));
        }

        // ✅ Set the total forecast in the TextView instead of the list
        forecastTotalTextView.setText("Total Forecast: " + totalForecast + " units");
    }


    // Function to handle the Next Month forecast (First to Last day of next month)
    private void forecastForNextMonth(Map<String, List<Integer>> categoryQuantities) {
        forecastCategoryList.clear();

        int totalHistorical = categoryQuantities.values().stream()
                .flatMap(List::stream)
                .mapToInt(Integer::intValue)
                .sum();

        int totalForecast = forecastedQuantity;

        for (Map.Entry<String, List<Integer>> entry : categoryQuantities.entrySet()) {
            int categorySum = entry.getValue().stream().mapToInt(Integer::intValue).sum();
            int adjustedQuantity = totalHistorical == 0 ? 0 :
                    (int) Math.round((double) categorySum / totalHistorical * totalForecast);
            forecastCategoryList.add(new ForecastCategoryModel(entry.getKey(), adjustedQuantity));
        }

        // ✅ Show the total in a TextView instead of adding to the list
        forecastTotalTextView.setText("Total Forecast: " + totalForecast + " units");
    }



}
