package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ManageOrderDetailActivity extends AppCompatActivity {

    private TextView productName, quantity, totalPrice, orderStatus, deliveryOption, tvPaymentStatus, tvPaymentDetails;
    private ImageView productImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order_detail);

        // Initialize views
        productName = findViewById(R.id.product_name);
        quantity = findViewById(R.id.quantity);
        totalPrice = findViewById(R.id.total_price);
        orderStatus = findViewById(R.id.order_status);
        deliveryOption = findViewById(R.id.delivery_option);
        productImageView = findViewById(R.id.product_image);
        tvPaymentStatus = findViewById(R.id.payment_status);
        tvPaymentDetails = findViewById(R.id.payment_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Retrieve the data from the Intent
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        String productNameValue = intent.getStringExtra("productName");
        long productQuantity = intent.getLongExtra("productQuantity", 0);
        double productTotalPrice = intent.getDoubleExtra("totalPrice", 0.0);
        String orderStatusValue = intent.getStringExtra("orderStatus");
        String deliveryOptionValue = intent.getStringExtra("deliveryOption");
        String imageUrl = intent.getStringExtra("imageUrl");
        String paymentIntentId = intent.getStringExtra("paymentIntentId");

        // Set the data to the views
        productName.setText("Product Name: " + productNameValue);
        quantity.setText("Quantity: " + productQuantity);
        totalPrice.setText("Total Price: ₱" + productTotalPrice);
        orderStatus.setText("Status: " + orderStatusValue);
        deliveryOption.setText("Delivery Method: " + deliveryOptionValue);

        // Load the product image using Glide
        Glide.with(this)
                .load(imageUrl)
                .into(productImageView);

        // Fetch payment summary
        if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
            fetchPaymentSummary(paymentIntentId);
        } else {
            // If paymentIntentId is not available, display default message
            tvPaymentStatus.setText("Payment Status: Not Available");
            tvPaymentDetails.setText("Receipt not available.");
        }
    }

    private void fetchPaymentSummary(String paymentIntentId) {
        String urlString = "https://payment-summary-git-master-eduud23s-projects.vercel.app/payment-summary?payment_intent_id=" + paymentIntentId;

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the response
                JSONObject jsonResponse = new JSONObject(response.toString());
                String receiptUrl = jsonResponse.optString("receipt_url", "Not Available");

                // Update UI on the main thread
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("Payment Status: Success");
                    tvPaymentDetails.setText("Receipt URL: " + receiptUrl);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("Payment Status: Error fetching data");
                    tvPaymentDetails.setText("No receipt available");
                });
            }
        }).start();
    }
}
