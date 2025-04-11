package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
    private String clientSecret;
    private final String secretKey = "sk_test_51PF3ByC6MmcIFikTxmE9dhgo5ZLxCWlNgqBaBMwZUKCCeRd0pkgKBQZOBO9UymYma2sNPpNIKlU2befDh0JeISU700OoXXptWX";
    private final String publishableKey = "pk_test_51PF3ByC6MmcIFikTjKhzCftwVaWmffD2iAqfquBroHxyujRLOG6QJ07t0tljO8FzDYbsNZld6sSjbTSTFUfT8J1c00D2b0tfvg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        payButton = findViewById(R.id.btn_payment);

        // Initialize Stripe
        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Set up button click listener
        payButton.setOnClickListener(v -> {
            Log.d("DeliveryInfoActivity", "Pay button clicked");
            paymentFlow(100.00);  // Sample amount, replace it with the actual amount
        });
    }

    // Start payment flow directly
    private void paymentFlow(double amount) {
        createPaymentIntent(amount);  // Create the PaymentIntent
    }

    // Create a PaymentIntent to get a clientSecret
    private void createPaymentIntent(double amount) {
        // Simulating the PaymentIntent creation on your backend (server-side)
        // In your production environment, you should call your backend to create a PaymentIntent.

        // For now, we are simulating the server-side response here.
        // This should be replaced by an actual backend call.
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        clientSecret = object.getString("client_secret");
                        presentPaymentSheet();  // Show the payment sheet with the clientSecret
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
                headers.put("Authorization", "Bearer " + secretKey);  // Use your secret key here
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf((int) (amount * 100))); // Convert price to cents
                params.put("currency", "usd");  // Or any other currency
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    // Show the Stripe Payment Sheet after receiving the clientSecret
    private void presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(clientSecret, new PaymentSheet.Configuration("Your Company Name"));
    }

    // Handle the result of the payment
    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d("PaymentResult", "Payment successful");
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();

            // Optionally, you can proceed with storing the order or navigating to another screen
            finish();
            Intent intent = new Intent(DeliveryInfoActivity.this, HomePageBuyer.class);
            startActivity(intent);  // Navigate to HomePageBuyer after payment
        } else {
            Log.d("PaymentResult", "Payment failed");
            Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
        }
    }
}
