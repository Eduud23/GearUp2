package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
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

    private EditText shippingAddressEditText, zipCodeEditText, riderMessageEditText;
    private RadioButton radioDelivery, radioPickup;
    private List<CartItem> cartItems;

    private EditText voucherEditText;
    private Button validateVoucherButton;

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

        shippingAddressEditText = findViewById(R.id.shipping_address);
        zipCodeEditText = findViewById(R.id.zip_code);
        riderMessageEditText = findViewById(R.id.rider_message);
        radioDelivery = findViewById(R.id.radio_delivery);
        radioPickup = findViewById(R.id.radio_pickup);

        voucherEditText = findViewById(R.id.voucher); // Assuming you have a voucher input field
        validateVoucherButton = findViewById(R.id.btn_validate_voucher);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        validateVoucherButton.setOnClickListener(v -> {
            String voucherCode = voucherEditText.getText().toString().trim();
            if (!voucherCode.isEmpty()) {
                validateVoucher(voucherCode); // Call the validation method
            } else {
                Toast.makeText(this, "Please enter a voucher code", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener deliveryOptionChangeListener = v -> {
            boolean isDelivery = radioDelivery.isChecked();

            shippingAddressEditText.setVisibility(isDelivery ? View.VISIBLE : View.GONE);
            zipCodeEditText.setVisibility(isDelivery ? View.VISIBLE : View.GONE);
            riderMessageEditText.setVisibility(isDelivery ? View.VISIBLE : View.GONE);
        };

        radioDelivery.setOnClickListener(deliveryOptionChangeListener);
        radioPickup.setOnClickListener(deliveryOptionChangeListener);

// Trigger once to ensure correct visibility on startup
        deliveryOptionChangeListener.onClick(radioDelivery);

        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormCompletion(); // Validate fields on change
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

// Attach to the text fields
        ((EditText) findViewById(R.id.email)).addTextChangedListener(formWatcher);
        ((EditText) findViewById(R.id.fullName)).addTextChangedListener(formWatcher);
        ((EditText) findViewById(R.id.phone_number)).addTextChangedListener(formWatcher);
        shippingAddressEditText.addTextChangedListener(formWatcher);
        zipCodeEditText.addTextChangedListener(formWatcher);

// Also check when toggling between Delivery and Pickup
        radioDelivery.setOnClickListener(v -> {
            updateDeliveryFieldsVisibility(true);
            checkFormCompletion();
        });
        radioPickup.setOnClickListener(v -> {
            updateDeliveryFieldsVisibility(false);
            checkFormCompletion();
        });

// Call it once on startup in case fields are already filled
        checkFormCompletion();

        RecyclerView recyclerViewCheckout = findViewById(R.id.recyclerView_checkout);
        recyclerViewCheckout.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        String totalAmount = getIntent().getStringExtra("totalAmount");

        if (totalAmount != null) {
            totalAmount = totalAmount.replaceAll("[^\\d.]", ""); // Remove currency symbols
            finalPrice = Double.parseDouble(totalAmount);

            // Format the price with commas and two decimal places
            DecimalFormat formatter = new DecimalFormat("#,###,###.00");
            String formattedPrice = formatter.format(finalPrice);

            // Display the formatted price with the currency symbol
            totalAmountText.setText("₱" + formattedPrice);
        }
        CheckoutAdapter checkoutAdapter = new CheckoutAdapter(cartItems);
        recyclerViewCheckout.setAdapter(checkoutAdapter);

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

    private void checkFormCompletion() {
        String email = ((EditText) findViewById(R.id.email)).getText().toString().trim();
        String fullName = ((EditText) findViewById(R.id.fullName)).getText().toString().trim();
        String phone = ((EditText) findViewById(R.id.phone_number)).getText().toString().trim();
        boolean isDelivery = radioDelivery.isChecked();

        // Required fields for both Delivery and Pickup
        boolean allFilled = !email.isEmpty() && !fullName.isEmpty() && !phone.isEmpty();

        // If delivery is selected, also require address fields
        if (isDelivery) {
            String address = shippingAddressEditText.getText().toString().trim();
            String zip = zipCodeEditText.getText().toString().trim();

            allFilled = allFilled && !address.isEmpty() && !zip.isEmpty();
        }

        // Show or hide the Stripe button
        btnStripePayment.setVisibility(allFilled ? View.VISIBLE : View.GONE);
    }
    private void updateDeliveryFieldsVisibility(boolean isVisible) {
        shippingAddressEditText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        zipCodeEditText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        riderMessageEditText.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void validateVoucher(String voucherCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("voucher")
                .whereEqualTo("code", voucherCode) // Query for the voucher code
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            // Voucher exists, apply discount
                            for (DocumentSnapshot document : task.getResult()) {
                                String discountStr = document.getString("discount"); // Retrieve the discount value as a string
                                Log.d("VoucherValidation", "Voucher code: " + voucherCode + " found with discount: " + discountStr);

                                if (discountStr != null && !discountStr.isEmpty()) {
                                    // Check if the discount is a percentage or flat amount
                                    if (discountStr.contains("%")) {
                                        // If it's a percentage discount
                                        double discount = parsePercentageDiscount(discountStr);
                                        if (discount >= 0) {
                                            applyPercentageDiscount(discount); // Apply the percentage discount
                                            Toast.makeText(this, "Voucher applied! Discount: " + discountStr, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Invalid percentage value in voucher", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // If it's a flat discount (numeric value)
                                        double discount = parseFlatDiscount(discountStr);
                                        if (discount >= 0) {
                                            applyFlatDiscount(discount); // Apply the flat discount
                                            Toast.makeText(this, "Voucher applied! Discount: ₱" + discount, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Invalid discount value in voucher", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.d("VoucherValidation", "Voucher code: " + voucherCode + " not found.");
                            Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("VoucherValidation", "Error validating voucher: " + task.getException().getMessage());
                        Toast.makeText(this, "Error validating voucher", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to parse a percentage discount (e.g., "20%")
    private double parsePercentageDiscount(String discountStr) {
        try {
            // Remove the '%' character and parse the percentage as a double
            discountStr = discountStr.replace("%", "").trim();
            return Double.parseDouble(discountStr);
        } catch (NumberFormatException e) {
            Log.e("VoucherValidation", "Error parsing percentage discount: " + e.getMessage());
            return -1; // Return -1 if there's an error in parsing
        }
    }

    // Helper method to parse a flat discount (e.g., "20")
    private double parseFlatDiscount(String discountStr) {
        try {
            // Simply parse the discount as a flat numeric value
            return Double.parseDouble(discountStr);
        } catch (NumberFormatException e) {
            Log.e("VoucherValidation", "Error parsing flat discount: " + e.getMessage());
            return -1; // Return -1 if there's an error in parsing
        }
    }

    // Function to apply a percentage discount to the final price
    private void applyPercentageDiscount(double percentage) {
        // Calculate the discount amount based on the total price
        double discountAmount = finalPrice * (percentage / 100);

        // Reduce the final price by the calculated discount amount
        finalPrice -= discountAmount;

        // Update the button text with the new price after discount
        btnStripePayment.setText("Pay with Stripe (₱" + finalPrice + ")");
        Log.d("DiscountApplied", "Final price after " + percentage + "% discount: ₱" + finalPrice);
    }

    // Function to apply a flat discount to the final price
    private void applyFlatDiscount(double discount) {
        // Reduce the final price by the flat discount
        finalPrice -= discount;

        // Update the button text with the new price after discount
        btnStripePayment.setText("Pay with Stripe (₱" + finalPrice + ")");
        Log.d("DiscountApplied", "Final price after ₱" + discount + " discount: ₱" + finalPrice);
    }


}
