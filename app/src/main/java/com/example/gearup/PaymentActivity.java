package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView totalAmountText;
    private EditText shippingAddress, email, fullName, phoneNumber, zipCode, riderMessage;
    private RadioGroup deliveryOption;
    private Button btnStripePayment;
    private double finalPrice;
    private List<CartItem> cartItems;

    private PaymentSheet paymentSheet;
    private String clientSecret;
    private String paymentIntentId;
    private final String secretKey = "sk_test_51PF3ByC6MmcIFikTxmE9dhgo5ZLxCWlNgqBaBMwZUKCCeRd0pkgKBQZOBO9UymYma2sNPpNIKlU2befDh0JeISU700OoXXptWX";
    private final String publishableKey = "pk_test_51PF3ByC6MmcIFikTjKhzCftwVaWmffD2iAqfquBroHxyujRLOG6QJ07t0tljO8FzDYbsNZld6sSjbTSTFUfT8J1c00D2b0tfvg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        totalAmountText = findViewById(R.id.textView_total_payment);
        email = findViewById(R.id.email);
        fullName = findViewById(R.id.fullName);
        phoneNumber = findViewById(R.id.phone_number);
        zipCode = findViewById(R.id.zip_code);
        riderMessage = findViewById(R.id.rider_message);
        shippingAddress = findViewById(R.id.shipping_address);
        deliveryOption = findViewById(R.id.delivery_option);
        btnStripePayment = findViewById(R.id.btn_stripe_payment);


        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        String totalAmount = getIntent().getStringExtra("totalAmount");

        if (totalAmount != null) {
            totalAmount = totalAmount.replaceAll("[^\\d.]", "");
            finalPrice = Double.parseDouble(totalAmount);
            totalAmountText.setText("₱" + totalAmount);
        }

        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        btnStripePayment.setText("Pay with Stripe (₱" + finalPrice + ")");
        btnStripePayment.setOnClickListener(v -> createPaymentIntent(finalPrice));
    }

    private void createPaymentIntent(double amount) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        clientSecret = object.getString("client_secret");
                        paymentIntentId = object.getString("id");
                        presentPaymentSheet();
                    } catch (JSONException e) {
                        Log.e("StripeError", "Error parsing payment intent: " + e.getMessage());
                    }
                }, error -> {
            Toast.makeText(this, "Payment Intent creation failed", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + secretKey);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("amount", String.valueOf((int) (amount * 100))); // Stripe accepts smallest currency unit
                params.put("currency", "php");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(clientSecret,
                new PaymentSheet.Configuration("GearUp"));
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
            saveOrderToFirestore();
        } else {
            Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderToFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String emailStr = email.getText().toString().trim();
        String fullNameStr = fullName.getText().toString().trim();
        String phoneNumberStr = phoneNumber.getText().toString().trim();
        String zipCodeStr = zipCode.getText().toString().trim();
        String riderMsgStr = riderMessage.getText().toString().trim();
        String address = shippingAddress.getText().toString().trim();

        int selectedOption = deliveryOption.getCheckedRadioButtonId();
        String deliveryType = selectedOption == R.id.radio_delivery ? "Delivery" : "Pickup";

        for (CartItem item : cartItems) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", item.getProductId());
            product.put("productName", item.getProductName());
            product.put("productYear", item.getYearModel());
            product.put("productQuantity", item.getQuantity());
            product.put("sellerId", item.getSellerId());
            product.put("userId", userId);
            product.put("paymentMethod", "Stripe");
            product.put("totalPrice", item.getTotalPrice());
            product.put("paymentIntentId", paymentIntentId);
            product.put("imageUrl", item.getImageUrl());

            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("product", product);
            orderDetails.put("deliveryType", deliveryType);
            if (deliveryType.equals("Delivery")) {
                orderDetails.put("shippingAddress", address);
            }
            orderDetails.put("customerInfo", new HashMap<String, Object>() {{
                put("email", emailStr);
                put("fullName", fullNameStr);
                put("phoneNumber", phoneNumberStr);
                put("zipCode", zipCodeStr);
                put("riderMessage", riderMsgStr);
            }});
            orderDetails.put("status", "Pending");

            db.collection("orders").add(orderDetails)
                    .addOnSuccessListener(documentReference -> {
                        deleteCartItems(userId);
                        showCustomDialog(true);
                    })
                    .addOnFailureListener(e -> showCustomDialog(false));
        }
    }

    private void showCustomDialog(boolean isSuccess) {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.dialogButton);
        ImageView icon = dialogView.findViewById(R.id.dialogIcon);

        if (isSuccess) {
            title.setText("Payment Successful");
            message.setText("Your order has been confirmed.");
            icon.setImageResource(R.drawable.success);
        } else {
            title.setText("Payment Failed");
            message.setText("Something went wrong. Please try again.");
            icon.setImageResource(R.drawable.error);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (isSuccess) {
                finish();
            }
        });
    }

    private void deleteCartItems(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("buyers").document(userId).collection("cartItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        db.collection("buyers").document(userId).collection("cartItems").document(doc.getId()).delete();
                    }
                });

        db.collection("sellers").document(userId).collection("cartItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        db.collection("sellers").document(userId).collection("cartItems").document(doc.getId()).delete();
                    }
                });
    }

}
