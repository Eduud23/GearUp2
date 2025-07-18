package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeliveryInfoActivity extends AppCompatActivity {
    private Button payButton;
    private PaymentSheet paymentSheet;
    private String customerId;
    private String ephemeralKey;
    private String clientSecret;
    private final String secretKey = "sk_test_51PF3ByC6MmcIFikTxmE9dhgo5ZLxCWlNgqBaBMwZUKCCeRd0pkgKBQZOBO9UymYma2sNPpNIKlU2befDh0JeISU700OoXXptWX";
    private final String publishableKey = "pk_test_51PF3ByC6MmcIFikTjKhzCftwVaWmffD2iAqfquBroHxyujRLOG6QJ07t0tljO8FzDYbsNZld6sSjbTSTFUfT8J1c00D2b0tfvg";
    private FirebaseFirestore db;
    private String buyerId;

    private EditText etName, etDeliveryAddress, etContactNumber, etZipCode;
    private RadioGroup radioGroupShipping;
    private RadioButton rbPickUp, rbDelivery;

    private Product product; // Holds product info
    private String productImageUrl; // Holds the first product image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed(); // Go back when the back button is clicked
        });


        etName = findViewById(R.id.et_name);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etContactNumber = findViewById(R.id.et_contact_number);
        etZipCode = findViewById(R.id.et_zip_code);
        payButton = findViewById(R.id.btn_payment);
        radioGroupShipping = findViewById(R.id.radio_group_shipping);
        rbPickUp = findViewById(R.id.rb_pickup);
        rbDelivery = findViewById(R.id.rb_delivery);

        db = FirebaseFirestore.getInstance();

        // Get the current user's ID (buyerId)
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            buyerId = currentUser.getUid(); // Use buyerId instead of userId
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve product from Intent
        product = getIntent().getParcelableExtra("PRODUCT");

        if (product == null || product.getImageUrls() == null || product.getImageUrls().isEmpty()) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get the first image URL
        productImageUrl = product.getImageUrls().get(0);

        // Initialize Stripe
        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Set up button click listener
        payButton.setOnClickListener(v -> {
            Log.d("DeliveryInfoActivity", "Pay button clicked");
            if (validateForm()) {
                paymentFlow(product.getPrice());
            }
        });

        // Ensure the button is enabled
        payButton.setEnabled(true);
        Log.d("DeliveryInfoActivity", "Button is enabled: " + payButton.isEnabled());

        // Set up radio group listener
        radioGroupShipping.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_pickup) {
                // If Pick-Up is selected, disable the Delivery Address input field
                etDeliveryAddress.setEnabled(false);
            } else if (checkedId == R.id.rb_delivery) {
                // If Delivery is selected, enable the Delivery Address input field
                etDeliveryAddress.setEnabled(true);
            }
        });

        // Initialize the Delivery Address field based on the initial state of the radio button
        if (rbPickUp.isChecked()) {
            etDeliveryAddress.setEnabled(false); // If Pick-Up is selected by default, disable the Delivery Address
        } else {
            etDeliveryAddress.setEnabled(true); // If Delivery is selected, enable the Delivery Address
        }
    }

    // Validate required fields before initiating payment
    private boolean validateForm() {
        // Validate if the required fields are filled
        if (etName.getText().toString().isEmpty() || etContactNumber.getText().toString().isEmpty() ||
                etZipCode.getText().toString().isEmpty() || (rbDelivery.isChecked() && etDeliveryAddress.getText().toString().isEmpty())) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void paymentFlow(double productPrice) {
        createCustomer(productPrice);
    }

    private void createCustomer(double productPrice) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/customers",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        customerId = object.getString("id");
                        getEphemeralKey(productPrice);
                    } catch (JSONException e) {
                        Log.e("StripeError", "Error creating customer: " + e.getMessage());
                    }
                }, error -> {
            Log.e("StripeError", "Error creating customer: " + error.getLocalizedMessage());
            Toast.makeText(DeliveryInfoActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + secretKey);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void getEphemeralKey(double productPrice) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/ephemeral_keys",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        ephemeralKey = object.getString("id");
                        createPaymentIntent(productPrice);
                    } catch (JSONException e) {
                        Log.e("StripeError", "Error creating ephemeral key: " + e.getMessage());
                    }
                }, error -> {
            Log.e("StripeError", "Error creating ephemeral key: " + error.getLocalizedMessage());
            Toast.makeText(DeliveryInfoActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + secretKey);
                headers.put("Stripe-Version", "2024-09-30.acacia");
                return headers;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void createPaymentIntent(double productPrice) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        clientSecret = object.getString("client_secret");
                        presentPaymentSheet();
                    } catch (JSONException e) {
                        Log.e("StripeError", "Error creating payment intent: " + e.getMessage());
                    }
                }, error -> {
            Log.e("StripeError", "Error creating payment intent: " + error.getLocalizedMessage());
            Toast.makeText(DeliveryInfoActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + secretKey);
                return headers;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf((int) (productPrice * 100))); // Convert price to cents
                params.put("currency", "php");
                params.put("customer", customerId);
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(clientSecret, new PaymentSheet.Configuration("Your Company Name"));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d("PaymentResult", "Payment successful");
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
            storeOrder();  // Store the order after payment is successful

            // Finish DeliveryInfoActivity to remove it from the back stack
            finish();

            // Start HomePageBuyer activity
            Intent intent = new Intent(DeliveryInfoActivity.this, HomePageBuyer.class);
            startActivity(intent);  // Navigate to HomePageBuyer
        } else {
            Log.d("PaymentResult", "Payment failed");
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void storeOrder() {
        String orderId = db.collection("orders").document().getId(); // Generate a new ID for the order
        Log.d("OrderDetails", "Generated Order ID: " + orderId);

        // Get user input for order details
        String userName = etName.getText().toString();
        String deliveryAddress = rbPickUp.isChecked() ? null : etDeliveryAddress.getText().toString();  // If Pick-Up is selected, deliveryAddress is null
        String contactNumber = etContactNumber.getText().toString();
        String zipCode = etZipCode.getText().toString();
        String shippingMethod = rbPickUp.isChecked() ? "Pick-Up" : "Delivery";  // Store the selected shipping method

        // Create the order with the status set to "Pending"
        Order order = new Order(
                orderId,
                buyerId, // Use buyerId here
                product.getPrice(),
                product.getName(),
                product.getBrand(),
                product.getYearModel(),
                product.getDescription(),
                getIntent().getIntExtra("PRODUCT_QUANTITY", 1),
                productImageUrl,
                userName,
                deliveryAddress,
                contactNumber,
                zipCode,
                "Pending", // Set the order status to "Pending"
                product.getSellerId(), // Store the sellerId
                shippingMethod // Store the selected shipping method
        );

        Log.d("OrderDetails", "Order object: " + order.toString());

        // Store the order in the 'orders' collection
        db.collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeliveryInfoActivity", "Order stored successfully");
                    // Corrected call to createNotification with buyerId as well
                    createNotification(orderId, userName, product.getName(), shippingMethod, product.getSellerId(), buyerId);
                })
                .addOnFailureListener(e -> {
                    Log.e("DeliveryInfoActivity", "Error storing order", e);
                });
    }


    // Create a notification in the 'notifications' collection
    private void createNotification(String orderId, String userName, String productName, String shippingMethod, String sellerId, String buyerId) {
        // Create a new document in the 'notifications' collection
        String notificationId = db.collection("notifications").document().getId(); // Auto-generated ID for the notification

        // Prepare the content of the notification message
        String message = "New order placed by " + userName + "\nProduct: " + productName;

        // Prepare data for the 'ordernotification' subcollection
        Map<String, Object> orderNotification = new HashMap<>();
        orderNotification.put("message", message);
        orderNotification.put("buyerId", buyerId); // Store buyerId
        orderNotification.put("orderId", orderId);  // Store orderId
        orderNotification.put("timestamp", System.currentTimeMillis()); // Timestamp when notification is created
        orderNotification.put("sellerId", sellerId); // Store sellerId as well

        // Store the notification in the 'notifications' collection under the generated notificationId
        db.collection("notifications")
                .document(notificationId)  // Reference to the notification document
                .collection("ordernotification") // Create the 'ordernotification' subcollection
                .document()  // Create a new document under 'ordernotification'
                .set(orderNotification) // Store the notification content
                .addOnSuccessListener(aVoid -> Log.d("DeliveryInfoActivity", "Notification stored successfully"))
                .addOnFailureListener(e -> Log.e("DeliveryInfoActivity", "Error storing notification", e));
    }

}
