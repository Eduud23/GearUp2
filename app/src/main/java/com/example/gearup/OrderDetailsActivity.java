package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OrderDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve the data passed from the previous activity
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        String productName = intent.getStringExtra("productName");
        long productQuantity = intent.getLongExtra("productQuantity", 0);
        double productPrice = intent.getDoubleExtra("productPrice", 0.0);
        String shippingAddress = intent.getStringExtra("shippingAddress");
        String deliveryOption = intent.getStringExtra("deliveryOption");
        String orderStatus = intent.getStringExtra("orderStatus");
        String imageUrl = intent.getStringExtra("imageUrl");
        String sellerId = intent.getStringExtra("sellerId");
        String paymentIntentId = intent.getStringExtra("paymentIntentId");

        // Get references to the UI elements
        TextView tvProductName = findViewById(R.id.tv_product_name);
        TextView tvProductQuantity = findViewById(R.id.tv_product_quantity);
        TextView tvProductPrice = findViewById(R.id.tv_product_price);
        TextView tvShopName = findViewById(R.id.tv_shop_name);  // New TextView for shop name
        TextView tvShippingAddress = findViewById(R.id.tv_shipping_address);
        TextView tvDeliveryOption = findViewById(R.id.tv_delivery_option);
        TextView tvOrderStatus = findViewById(R.id.tv_order_status);
        TextView tvPaymentStatus = findViewById(R.id.tv_payment_status); // New TextView for payment status
        TextView tvPaymentDetails = findViewById(R.id.tv_payment_details); // TextView to show all payment details
        ImageView ivProductImage = findViewById(R.id.iv_product_image);

        // Set the data to the UI elements
        tvProductName.setText(productName);
        tvProductQuantity.setText("Quantity: " + productQuantity);
        tvProductPrice.setText(String.format("₱%.2f", productPrice));
        tvShippingAddress.setText("Shipping Address: " + shippingAddress);
        tvDeliveryOption.setText("Delivery Option: " + deliveryOption);
        tvOrderStatus.setText("Order Status: " + orderStatus);

        // Load the product image using Glide
        Glide.with(this).load(imageUrl).into(ivProductImage);

        // Fetch the payment summary if paymentIntentId is available
        if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
            fetchPaymentSummary(paymentIntentId, tvPaymentStatus, tvPaymentDetails);
        }

        // Check if sellerId is null before fetching shop name
        if (sellerId != null && !sellerId.isEmpty()) {
            fetchShopName(sellerId, tvShopName);
        } else {
            tvShopName.setText("Shop: Not Available");
        }
    }

    private void fetchPaymentSummary(String paymentIntentId, TextView tvPaymentStatus, TextView tvPaymentDetails) {
        // Define the URL to send the GET request
        String urlString = "https://payment-summary-git-master-eduud23s-projects.vercel.app/payment-summary?payment_intent_id=" + paymentIntentId;

        // Perform the network request on a background thread
        new Thread(() -> {
            try {
                // Set up the connection
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.optString("status", "Unknown");
                double amount = jsonResponse.optDouble("amount", 0.0);
                String currency = jsonResponse.optString("currency", "USD");
                String cardBrand = jsonResponse.optString("card_brand", "Unknown");
                String cardLast4 = jsonResponse.optString("card_last4", "****");
                int expMonth = jsonResponse.optInt("card_exp_month", 0);
                int expYear = jsonResponse.optInt("card_exp_year", 0);
                String paymentMethodId = jsonResponse.optString("payment_method_id", "Unknown");
                String receiptUrl = jsonResponse.optString("receipt_url", "Not Available");

                // Update the UI with the payment details
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("Payment Status: " + status);
                    String paymentDetails = "Amount: " + amount + " " + currency + "\n" +
                            "Card: " + cardBrand + " " + cardLast4 + "\n" +
                            "Expiration: " + expMonth + "/" + expYear + "\n" +
                            "Payment Method ID: " + paymentMethodId + "\n" +
                            "Receipt: " + receiptUrl;
                    tvPaymentDetails.setText(paymentDetails);

                    // Optionally, show a Toast with summarized payment info
                    Toast.makeText(OrderDetailsActivity.this, "Payment Summary Loaded", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    tvPaymentStatus.setText("Payment Status: Error fetching data");
                });
            }
        }).start();
    }

    private void fetchShopName(String sellerId, TextView tvShopName) {
        // Ensure the sellerId is not null or empty
        if (sellerId == null || sellerId.isEmpty()) {
            tvShopName.setText("Shop: Not Available");
            return;
        }

        // Fetch the document using sellerId
        db.collection("sellers")  // Access the "sellers" collection
                .document(sellerId)  // Use the sellerId to find the document
                .get()  // Retrieve the document
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Retrieve the shopName from the document
                            String shopName = documentSnapshot.getString("shopName");
                            if (shopName != null && !shopName.isEmpty()) {
                                tvShopName.setText("Shop: " + shopName);  // Display shop name
                            } else {
                                tvShopName.setText("Shop: N/A");  // In case shopName is null or empty
                            }
                        } else {
                            tvShopName.setText("Shop: Not found");  // In case the document does not exist
                        }
                    } else {
                        tvShopName.setText("Shop: Error fetching data");  // In case of failure
                    }
                });
    }
}
