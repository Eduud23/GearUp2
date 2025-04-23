package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ManageOrderDetailActivity extends AppCompatActivity {

    private TextView productName, quantity, totalPrice, orderStatus, deliveryOption, tvPaymentStatus, tvPaymentDetails, productBrandView, productYearView;
    private ImageView productImageView;
    private TextView customerEmail, customerFullName, customerPhoneNumber, customerRiderMessage, customerZipCode;
    private FirebaseFirestore db;

    // Customer info layout
    private View customerInfoLayout;

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
        productBrandView = findViewById(R.id.product_brand);
        productYearView = findViewById(R.id.product_year);


        // Customer info views
        customerEmail = findViewById(R.id.customer_email);
        customerFullName = findViewById(R.id.customer_full_name);
        customerPhoneNumber = findViewById(R.id.customer_phone_number);
        customerRiderMessage = findViewById(R.id.customer_rider_message);
        customerZipCode = findViewById(R.id.customer_zip_code);

        // Customer info layout
        customerInfoLayout = findViewById(R.id.customer_info_layout);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Retrieve the data from the Intent
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        String productNameValue = intent.getStringExtra("productName");
        String productBrand = intent.getStringExtra("productBrand");
        String productYear = intent.getStringExtra("productYear");
        long productQuantity = intent.getLongExtra("productQuantity", 0);
        double productTotalPrice = intent.getDoubleExtra("totalPrice", 0.0);
        String orderStatusValue = intent.getStringExtra("orderStatus");
        String deliveryOptionValue = intent.getStringExtra("deliveryOption");
        String imageUrl = intent.getStringExtra("imageUrl");
        String paymentIntentId = intent.getStringExtra("paymentIntentId");

        // Set the data to the views
        productName.setText("Product Name: " + productNameValue);
        quantity.setText("Quantity: " + productQuantity);
        productBrandView.setText("Brand: " + productBrand);
        productYearView.setText("Year Model: " + productYear);
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

        // If the delivery option is "Delivery", fetch the customer info and make it visible
        if ("Delivery".equalsIgnoreCase(deliveryOptionValue)) {
            customerInfoLayout.setVisibility(View.VISIBLE);  // Show customer info layout
            fetchCustomerInfo(orderId);
        } else {
            customerInfoLayout.setVisibility(View.GONE);  // Hide customer info layout
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
                    tvPaymentDetails.setText(receiptUrl);
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

    private void fetchCustomerInfo(String orderId) {
        // Firestore reference to the order document using the orderId
        DocumentReference orderRef = db.collection("orders").document(orderId);

        orderRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Extract customer info from Firestore
                    String email = document.getString("customerInfo.email");
                    String fullName = document.getString("customerInfo.fullName");
                    String phoneNumber = document.getString("customerInfo.phoneNumber");
                    String riderMessage = document.getString("customerInfo.riderMessage");
                    String zipCode = document.getString("customerInfo.zipCode");

                    // Update UI with customer info
                    customerEmail.setText("Email: " + email);
                    customerFullName.setText("Full Name: " + fullName);
                    customerPhoneNumber.setText("Phone Number: " + phoneNumber);
                    customerRiderMessage.setText("Rider Message: " + riderMessage);
                    customerZipCode.setText("Zip Code: " + zipCode);
                } else {
                    customerEmail.setText("Customer Info not available.");
                }
            } else {
                customerEmail.setText("Error fetching customer info");
            }
        });
    }
}
