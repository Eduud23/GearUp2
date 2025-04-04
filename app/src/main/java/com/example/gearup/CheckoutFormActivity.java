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

    private TextView productName, productBrand, productYear, productPrice, productQuantity, cardTypeTextView;
    private EditText shippingAddress, cardName, cardNumber, expiryDate, cvv, riderMessage, fullNameField, emailField, phoneNumberField, zipCodeField;
    private RadioGroup deliveryOption;
    private Button confirmPayment;
    private ImageView productImage;
    private double finalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_form);

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
        cardTypeTextView = findViewById(R.id.card_type);

        deliveryOption = findViewById(R.id.delivery_option);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

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


    // Add TextWatcher for card number formatting
        cardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int beforeLength;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String input = s.toString().replaceAll("\\s", ""); // Remove existing spaces
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" "); // Insert space after every 4 digits
                    }
                    formatted.append(input.charAt(i));
                }

                cardNumber.removeTextChangedListener(this);
                cardNumber.setText(formatted.toString());
                cardNumber.setSelection(formatted.length()); // Move cursor to the end
                cardNumber.addTextChangedListener(this);

                isFormatting = false;
            }
        });
        // Expiry Date Formatting (MM/YY)
        expiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("[^\\d]", ""); // Remove non-numeric
                StringBuilder formatted = new StringBuilder();

                if (input.length() > 2) {
                    formatted.append(input.substring(0, 2)).append("/");
                    if (input.length() > 4) {
                        formatted.append(input.substring(2, 4)); // MM/YY
                    } else {
                        formatted.append(input.substring(2));
                    }
                } else {
                    formatted.append(input);
                }

                expiryDate.removeTextChangedListener(this);
                expiryDate.setText(formatted.toString());
                expiryDate.setSelection(formatted.length());
                expiryDate.addTextChangedListener(this);

                isFormatting = false;
            }
        });

// CVV Formatting (Restrict to 3-4 digits based on card type)
        cvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String cardType = identifyCardType(cardNumber.getText().toString());
                int maxLength = cardType.equals("American Express") ? 4 : 3;

                if (s.length() > maxLength) {
                    cvv.setText(s.subSequence(0, maxLength));
                    cvv.setSelection(maxLength);
                }
            }
        });


        // Add listener to detect card type dynamically
        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String cardType = identifyCardType(s.toString());
                cardTypeTextView.setText("Card Type: " + cardType);
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
        String cardType = identifyCardType(cardNum);
        if (cardType.equals("Unknown")) {
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show();
            return;
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
        paymentDetails.put("cardType", cardType);
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




    private String identifyCardType(String cardNum) {
        String cleanCardNum = cardNum.replaceAll("\\s", ""); // Remove spaces for validation

        if (cleanCardNum.startsWith("4")) {
            return "Visa";
        } else if (cleanCardNum.matches("^5[1-5].*") || cleanCardNum.matches("^222[1-9].*") ||
                cleanCardNum.matches("^22[3-9].*") || cleanCardNum.matches("^2[3-6].*") ||
                cleanCardNum.matches("^27[01].*") || cleanCardNum.matches("^2720.*")) {
            return "MasterCard";
        } else if (cleanCardNum.startsWith("34") || cleanCardNum.startsWith("37")) {
            return "American Express";
        } else if (cleanCardNum.startsWith("6011") || cleanCardNum.matches("^622(12[6-9]|1[3-9]\\d|[2-8]\\d\\d|9[01]\\d|92[0-5]).*") ||
                cleanCardNum.matches("^64[4-9].*") || cleanCardNum.startsWith("65")) {
            return "Discover";
        } else {
            return "Unknown";
        }
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