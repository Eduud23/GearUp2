package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CheckoutFormActivity extends AppCompatActivity {

    // Your UI components
    private TextView productName, productBrand, productYear, productPrice, productQuantity;
    private EditText shippingAddress, cardName, cardNumber, expiryDate, cvv, riderMessage, fullNameField, emailField, phoneNumberField, zipCodeField;
    private RadioGroup deliveryOption;
    private Button confirmPayment;
    private ImageView productImage, cardTypeImageView;
    private double finalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_form);

        // Initialize your UI components (same as before)
        productName = findViewById(R.id.product_name);
        productBrand = findViewById(R.id.product_brand);
        productYear = findViewById(R.id.product_year);
        productPrice = findViewById(R.id.product_price);
        productQuantity = findViewById(R.id.product_quantity);
        productImage = findViewById(R.id.product_image);
        shippingAddress = findViewById(R.id.shipping_address);
        fullNameField = findViewById(R.id.fullName);
        cardName = findViewById(R.id.card_name);
        cardNumber = findViewById(R.id.card_number);
        phoneNumberField = findViewById(R.id.phone_number);
        zipCodeField = findViewById(R.id.zip_code);
        emailField = findViewById(R.id.email);
        expiryDate = findViewById(R.id.expiry_date);
        cvv = findViewById(R.id.cvv);
        riderMessage = findViewById(R.id.rider_message);
        confirmPayment = findViewById(R.id.confirm_payment);
        cardTypeImageView = findViewById(R.id.card_type_image);
        deliveryOption = findViewById(R.id.delivery_option);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        // Retrieve product data from the intent and set the UI
        Intent intent = getIntent();
        if (intent != null) {
            productName.setText(intent.getStringExtra("PRODUCT_NAME"));
            productBrand.setText(intent.getStringExtra("PRODUCT_BRAND"));
            productYear.setText(intent.getStringExtra("PRODUCT_YEAR_MODEL"));
            double price = intent.getDoubleExtra("PRODUCT_PRICE", 0.0);
            int quantity = intent.getIntExtra("PRODUCT_QUANTITY", 1);

            // Multiply price by quantity
            finalPrice = price * quantity;
            productPrice.setText("₱" + price);
            productQuantity.setText("Quantity: " + quantity);

            String imageUrl = intent.getStringExtra("PRODUCT_IMAGE");
            if (imageUrl != null) {
                Glide.with(this).load(imageUrl).into(productImage);
            }
        }

        // Update the confirm payment button text to show the final price
        confirmPayment.setText("Confirm Payment ( ₱" + finalPrice + ")");
        confirmPayment.setOnClickListener(v -> processPayment());

        // Use the CardHelper class for formatting
        CardHelper.setupCardNumberFormatting(cardNumber);
        CardHelper.setupExpiryDateFormatting(expiryDate);
        CardHelper.setupCvvFormatting(cvv, cardNumber);

        // Add listener to detect card type dynamically
        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Get the card type image resource
                int cardImageResId = CardHelper.identifyCardType(s.toString());

                // Update the ImageView with the corresponding card image
                cardTypeImageView.setImageResource(cardImageResId);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void processPayment() {
        String address = shippingAddress.getText().toString().trim();
        String nameOnCard = cardName.getText().toString().trim();
        String cardNum = cardNumber.getText().toString().trim();
        String expiry = expiryDate.getText().toString().trim();
        String cvvCode = cvv.getText().toString().trim();
        String message = riderMessage.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String fullName = fullNameField.getText().toString().trim();
        String phoneNumber = phoneNumberField.getText().toString().trim();
        String zipCode = zipCodeField.getText().toString().trim();

        int selectedOption = deliveryOption.getCheckedRadioButtonId();
        String deliveryType = selectedOption == R.id.radio_delivery ? "Delivery" : "Pickup";

        // Validate input fields
        if (cardNum.isEmpty() || expiry.isEmpty() || cvvCode.isEmpty() || nameOnCard.isEmpty() ||
                email.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty() || zipCode.isEmpty() ||
                (selectedOption == R.id.radio_delivery && address.isEmpty())) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Identify card type
        int cardTypeImageResId = CardHelper.identifyCardType(cardNum); // Now returns an int (image resource ID)
        if (cardTypeImageResId == R.drawable.unknown) { // If the card type is "Unknown", handle it accordingly
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show();
            return;  // Do not proceed with payment
        }

        // Get user and seller IDs
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        String sellerId = getIntent().getStringExtra("SELLER_ID");

        // Get the image URL from the intent
        String imageUrl = getIntent().getStringExtra("PRODUCT_IMAGE");

        // Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create the main order map
        Map<String, Object> product = new HashMap<>();
        product.put("userId", userId);
        product.put("sellerId", sellerId);
        product.put("productName", productName.getText().toString());
        product.put("quantity", Integer.parseInt(productQuantity.getText().toString().replace("Quantity: ", "")));
        product.put("totalPrice", finalPrice);
        product.put("deliveryType", deliveryType);
        product.put("imageUrl", imageUrl);

        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("product", product);
        orderDetails.put("deliveryType", deliveryType);

        if (deliveryType.equals("Delivery")) {
            orderDetails.put("shippingAddress", address);
        }
        orderDetails.put("status", "Pending");

        // Store customer info in a sub-map
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("email", email);
        customerInfo.put("fullName", fullName);
        customerInfo.put("phoneNumber", phoneNumber);
        customerInfo.put("zipCode", zipCode);
        customerInfo.put("riderMessage", message);
        orderDetails.put("customerInfo", customerInfo);

        // Store payment details in a sub-map
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("cardName", nameOnCard);
        paymentDetails.put("cardNumber", cardNum);
        paymentDetails.put("expiryDate", expiry);
        paymentDetails.put("cvv", cvvCode);
        paymentDetails.put("cardType", cardTypeImageResId); // Store the image resource ID, not the name
        orderDetails.put("payment", paymentDetails);

        // Save to Firestore
        db.collection("orders")
                .add(orderDetails)
                .addOnSuccessListener(documentReference -> {
                    // Log the purchase interaction
                    UserInteractionLogger.logPurchaseInteraction(
                            userId,
                            getIntent().getStringExtra("PRODUCT_ID"),
                            getIntent().getStringExtra("PRODUCT_NAME"),
                            sellerId,
                            finalPrice
                    );

                    showCustomDialog(true);
                })
                .addOnFailureListener(e -> {
                    showCustomDialog(false);
                });
    }



    private void showCustomDialog(boolean isSuccess) {
        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        TextView title = dialogView.findViewById(R.id.dialogTitle);
        TextView message = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.dialogButton);
        ImageView icon = dialogView.findViewById(R.id.dialogIcon);

        // Customize based on success or failure
        if (isSuccess) {
            title.setText("Payment Successful");
            message.setText("Your order has been confirmed.");
            icon.setImageResource(R.drawable.success);
        } else {
            title.setText("Payment Failed");
            message.setText("Something went wrong. Please try again.");
            icon.setImageResource(R.drawable.error);
        }

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        // Handle the button click
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (isSuccess) {
                finish();  // Close the activity after successful payment
            }
        });
    }

}