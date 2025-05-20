package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckoutFormActivity extends AppCompatActivity {

    private TextView productName, productBrand, productYear, productPrice, productQuantity;
    private ImageView productImage;
    private Button btnStripePayment;

    private double finalPrice;

    // Stripe
    private PaymentSheet paymentSheet;
    private EditText voucherEditText;
    private Button validateVoucherButton;
    private String clientSecret;
    private String paymentIntentId;

    private EditText shippingAddressEditText, zipCodeEditText, riderMessageEditText;
    private RadioButton radioDelivery, radioPickup;



    private final String secretKey = "sk_test_51PF3ByC6MmcIFikTxmE9dhgo5ZLxCWlNgqBaBMwZUKCCeRd0pkgKBQZOBO9UymYma2sNPpNIKlU2befDh0JeISU700OoXXptWX";
    private final String publishableKey = "pk_test_51PF3ByC6MmcIFikTjKhzCftwVaWmffD2iAqfquBroHxyujRLOG6QJ07t0tljO8FzDYbsNZld6sSjbTSTFUfT8J1c00D2b0tfvg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_form);

        // Initialize views
        productName = findViewById(R.id.product_name);
        productBrand = findViewById(R.id.product_brand);
        productYear = findViewById(R.id.product_year);
        productPrice = findViewById(R.id.product_price);
        productQuantity = findViewById(R.id.product_quantity);
        productImage = findViewById(R.id.product_image);
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

        fetchAndSetUserEmail();


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


        // Retrieve the product details from the intent
        Intent intent = getIntent();
        if (intent != null) {
            productName.setText(intent.getStringExtra("PRODUCT_NAME"));
            productBrand.setText(intent.getStringExtra("PRODUCT_BRAND"));
            productYear.setText(intent.getStringExtra("PRODUCT_YEAR_MODEL"));
            double price = intent.getDoubleExtra("PRODUCT_PRICE", 0.0);
            int quantity = intent.getIntExtra("PRODUCT_QUANTITY", 1);
            finalPrice = price * quantity;
            productPrice.setText("₱" + price);
            productQuantity.setText("Quantity: " + quantity);
            String imageUrl = intent.getStringExtra("PRODUCT_IMAGE");
            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(productImage);
            }
        }

        // Initialize Stripe payment configuration
        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        // Set up the Stripe payment button
        btnStripePayment.setText("Pay with Stripe (₱" + finalPrice + ")");
        btnStripePayment.setOnClickListener(v -> createPaymentIntent(finalPrice)); // Trigger Stripe payment when clicked
    }

    // Create a PaymentIntent to get client secret from your backend
    private void createPaymentIntent(double amount) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://api.stripe.com/v1/payment_intents",
                response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        clientSecret = object.getString("client_secret");
                        paymentIntentId = object.getString("id");
                        presentPaymentSheet();
                    } catch (JSONException e) {
                        Log.e("StripeError", "Error creating payment intent: " + e.getMessage());
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
                params.put("amount", String.valueOf((int) (amount * 100))); // in cents
                params.put("currency", "php");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // Show the Stripe Payment Sheet using the client secret
    private void presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(clientSecret,
                new PaymentSheet.Configuration("GearUp"));
    }

    // Handle the result of the payment
    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
            saveOrderToFirestore();
        } else {
            Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrderToFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve user inputs
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String fullName = ((EditText) findViewById(R.id.fullName)).getText().toString();
        String phoneNumber = ((EditText) findViewById(R.id.phone_number)).getText().toString();
        String shippingAddress = ((EditText) findViewById(R.id.shipping_address)).getText().toString();
        String zipCode = ((EditText) findViewById(R.id.zip_code)).getText().toString();
        String riderMessage = ((EditText) findViewById(R.id.rider_message)).getText().toString();
        String deliveryOption = ((RadioButton) findViewById(R.id.radio_delivery)).isChecked() ? "Delivery" : "Pickup";

        String quantityText = productQuantity.getText().toString();
        String[] parts = quantityText.split(":"); // Split the string based on ":"
        String quantityStr = parts.length > 1 ? parts[1].trim() : "1"; // Take the numeric part and trim any extra spaces
        int quantity = Integer.parseInt(quantityStr);

        String productId = getIntent().getStringExtra("PRODUCT_ID");
        // Retrieve the sellerId (assumed to be passed with product or from another source)
        String sellerId = getIntent().getStringExtra("SELLER_ID"); // Get sellerId from intent, if passed

        // Create the product structure with userId and imageUrl inside it
        Map<String, Object> product = new HashMap<>();
        product.put("productId", productId);
        product.put("productName", productName.getText().toString());
        product.put("productBrand", productBrand.getText().toString());
        product.put("productYear", productYear.getText().toString());
        product.put("productQuantity", quantity);
        product.put("totalPrice", finalPrice);
        product.put("paymentMethod", "Stripe");
        product.put("paymentIntentId", paymentIntentId);
        product.put("userId", userId); // Move userId inside product
        product.put("sellerId", sellerId); // Add sellerId to the product map
        String imageUrl = getIntent().getStringExtra("PRODUCT_IMAGE");
        if (imageUrl != null) {
            product.put("imageUrl", imageUrl); // Move imageUrl inside product
        }

        // Create the customer info structure
        Map<String, Object> customerInfo = new HashMap<String, Object>() {{
            put("email", email);
            put("fullName", fullName);
            put("phoneNumber", phoneNumber);
            put("zipCode", zipCode);
            put("riderMessage", riderMessage);
        }};

        // Create the order structure
        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("status", "Pending");
        orderDetails.put("shippingAddress", shippingAddress);
        orderDetails.put("deliveryType", deliveryOption);
        orderDetails.put("customerInfo", customerInfo);
        orderDetails.put("product", product); // Add the product map

        // Save the order to Firestore
        db.collection("orders")
                .add(orderDetails)
                .addOnSuccessListener(docRef -> {
                    // Log purchase interaction
                    UserInteractionLogger.logPurchaseInteraction(userId, productId, product.get("productName").toString(), sellerId, (double) product.get("totalPrice"));

                    updateProductQuantityInCollectionGroup(productId, quantity); // Deduct the purchased quantity

                    showCustomDialog(true);
                })

                .addOnFailureListener(e -> showCustomDialog(false));
    }



    // Show a custom dialog for the payment result
    private void showCustomDialog(boolean isSuccess) {
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.dialogButton);
        ImageView icon = dialogView.findViewById(R.id.dialogIcon);

        if (isSuccess) {
            title.setText("Payment Successful");
            message.setText("Your order has been confirmed.");
            icon.setImageResource(R.drawable.success);  // Assuming you have a success icon
        } else {
            title.setText("Payment Failed");
            message.setText("Something went wrong. Please try again.");
            icon.setImageResource(R.drawable.error);    // Assuming you have an error icon
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (isSuccess) {
                finish();
            }
        });
    }

    private void checkFormCompletion() {
        EditText phoneField = findViewById(R.id.phone_number);

        String email = ((EditText) findViewById(R.id.email)).getText().toString().trim();
        String fullName = ((EditText) findViewById(R.id.fullName)).getText().toString().trim();
        String phone = ((EditText) findViewById(R.id.phone_number)).getText().toString().trim();
        boolean isDelivery = radioDelivery.isChecked();


        boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean isPhoneValid = phone.matches("\\d{11}");

        if (!isEmailValid && !email.isEmpty()) {
            ((EditText) findViewById(R.id.email)).setError("Invalid email format");
        }

        if (!phone.isEmpty() && !isPhoneValid) {
            phoneField.setError("Phone number must be exactly 11 digits");
        }


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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // First, check if this user has already used this voucher
        db.collection("voucher_usages")
                .document(voucherCode + "_" + userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "You have already used this voucher", Toast.LENGTH_SHORT).show();
                    } else {
                        // Voucher not used yet, proceed to validate and apply
                        db.collection("voucher")
                                .whereEqualTo("code", voucherCode)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            String discountStr = document.getString("discount");
                                            String status = document.getString("status");

                                            if (status == null || !status.equalsIgnoreCase("valid")) {
                                                Toast.makeText(this, "This voucher is expired or invalid", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            if (discountStr != null && !discountStr.isEmpty()) {
                                                if (discountStr.contains("%")) {
                                                    double discount = parsePercentageDiscount(discountStr);
                                                    if (discount >= 0) {
                                                        applyPercentageDiscount(discount);
                                                    } else {
                                                        Toast.makeText(this, "Invalid percentage value", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                } else {
                                                    double discount = parseFlatDiscount(discountStr);
                                                    if (discount >= 0) {
                                                        applyFlatDiscount(discount);
                                                    } else {
                                                        Toast.makeText(this, "Invalid discount value", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                }

                                                // Record the usage
                                                Map<String, Object> usage = new HashMap<>();
                                                usage.put("userId", userId);
                                                usage.put("voucherCode", voucherCode);
                                                usage.put("usedAt", System.currentTimeMillis());

                                                db.collection("voucher_usages")
                                                        .document(voucherCode + "_" + userId)
                                                        .set(usage);

                                                Toast.makeText(this, "Voucher applied! Discount: " + discountStr, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking voucher usage", Toast.LENGTH_SHORT).show();
                    Log.e("VoucherValidation", "Error checking voucher usage: " + e.getMessage());
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
        double discountAmount = finalPrice * (percentage / 100);
        finalPrice -= discountAmount;
        finalPrice = Math.round(finalPrice * 100.0) / 100.0; // Round to 2 decimal places

        btnStripePayment.setText("Pay with Stripe (₱" + String.format("%.2f", finalPrice) + ")");
        Log.d("DiscountApplied", "Final price after " + percentage + "% discount: ₱" + finalPrice);
    }

    // Function to apply a flat discount to the final price
    private void applyFlatDiscount(double discount) {
        finalPrice -= discount;
        finalPrice = Math.round(finalPrice * 100.0) / 100.0; // Round to 2 decimal places

        btnStripePayment.setText("Pay with Stripe (₱" + String.format("%.2f", finalPrice) + ")");
        Log.d("DiscountApplied", "Final price after ₱" + discount + " discount: ₱" + finalPrice);
    }

    private void fetchAndSetUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference buyerRef = db.collection("buyers").document(userId);
        DocumentReference sellerRef = db.collection("sellers").document(userId);

        // Try to get from buyers
        buyerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");
                if (email != null) {
                    ((EditText) findViewById(R.id.email)).setText(email);
                }
            } else {
                // Try to get from sellers
                sellerRef.get().addOnSuccessListener(sellerDoc -> {
                    if (sellerDoc.exists()) {
                        String email = sellerDoc.getString("email");
                        if (email != null) {
                            ((EditText) findViewById(R.id.email)).setText(email);
                        }
                    } else {
                        Log.w("UserEmail", "Email not found in both buyers and sellers collection.");
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching from sellers: " + e.getMessage()));
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching from buyers: " + e.getMessage()));
    }

    private void updateProductQuantityInCollectionGroup(String productId, int purchasedQuantity) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collectionGroup("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean found = false;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.getId().equals(productId)) {
                            found = true;
                            DocumentReference productRef = document.getReference();
                            Long currentQuantity = document.getLong("quantity");

                            if (currentQuantity != null && currentQuantity >= purchasedQuantity) {
                                long updatedQuantity = currentQuantity - purchasedQuantity;
                                productRef.update("quantity", updatedQuantity)
                                        .addOnSuccessListener(aVoid -> Log.d("InventoryUpdate", "Product quantity updated successfully"))
                                        .addOnFailureListener(e -> Log.e("InventoryUpdate", "Failed to update product quantity: " + e.getMessage()));
                            } else {
                                Log.w("InventoryUpdate", "Not enough inventory or quantity field is missing.");
                            }
                            break;
                        }
                    }

                    if (!found) {
                        Log.w("InventoryUpdate", "Product with ID " + productId + " not found in collectionGroup 'products'");
                    }
                })
                .addOnFailureListener(e -> Log.e("InventoryUpdate", "Error querying collectionGroup: " + e.getMessage()));
    }






}