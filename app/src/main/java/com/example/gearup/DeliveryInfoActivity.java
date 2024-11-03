package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private String currentUserId;

    private EditText etName, etDeliveryAddress, etContactNumber, etZipCode;

    private Product product; // Holds product info
    private String productImageUrl; // Holds the first product image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info);

        etName = findViewById(R.id.et_name);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etContactNumber = findViewById(R.id.et_contact_number);
        etZipCode = findViewById(R.id.et_zip_code);
        payButton = findViewById(R.id.btn_payment);
        db = FirebaseFirestore.getInstance();

        // Get the current user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
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
            paymentFlow(product.getPrice());
        });

        // Ensure the button is enabled
        payButton.setEnabled(true);
        Log.d("DeliveryInfoActivity", "Button is enabled: " + payButton.isEnabled());
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
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(DeliveryInfoActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
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
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(DeliveryInfoActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
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
                        storeOrder(productPrice); // Store order after creating payment intent
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Toast.makeText(DeliveryInfoActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
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

    private void storeOrder(double productPrice) {
        String orderId = db.collection("orders").document().getId(); // Generate a new ID

        // Get user input
        String userName = etName.getText().toString();
        String deliveryAddress = etDeliveryAddress.getText().toString();
        String contactNumber = etContactNumber.getText().toString();
        String zipCode = etZipCode.getText().toString();

        // Create the order with order status set to "Pending"
        Order order = new Order(
                orderId,
                currentUserId,
                productPrice,
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
                "Pending", // Set order status to "Pending"
                product.getSellerId() // Store the sellerId
        );

        // Store the order in the 'orders' collection
        db.collection("orders")
                .document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> Log.d("DeliveryInfoActivity", "Order stored successfully"))
                .addOnFailureListener(e -> Log.e("DeliveryInfoActivity", "Error storing order", e));
    }

    private void presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(clientSecret, new PaymentSheet.Configuration("Your Company Name"));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }
}
