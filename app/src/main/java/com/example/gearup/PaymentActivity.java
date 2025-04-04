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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView totalAmountText, cardTypeTextView;
    private EditText shippingAddress, email, fullName, phoneNumber, zipCode, riderMessage;
    private EditText cardName, cardNumber, expiryDate, cvv;
    private RadioGroup deliveryOption;
    private Button confirmPaymentButton;
    private double finalPrice;
    private List<CartItem> cartItems;
    private boolean isFormatting;

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
        cardName = findViewById(R.id.card_name);
        cardNumber = findViewById(R.id.card_number);
        expiryDate = findViewById(R.id.expiry_date);
        cvv = findViewById(R.id.cvv);
        shippingAddress = findViewById(R.id.shipping_address);
        deliveryOption = findViewById(R.id.delivery_option);
        confirmPaymentButton = findViewById(R.id.confirm_payment);
        cardTypeTextView = findViewById(R.id.card_type);

        // Get data from intent
        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        String totalAmount = getIntent().getStringExtra("totalAmount");

        if (totalAmount != null) {
            totalAmount = totalAmount.replaceAll("[^\\d.]", ""); // Remove non-numeric characters
            finalPrice = Double.parseDouble(totalAmount);
            totalAmountText.setText("₱" + totalAmount);
        }

        confirmPaymentButton.setText("Confirm Payment ( ₱" + finalPrice + ")");
        confirmPaymentButton.setOnClickListener(v -> processPayment());

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

        expiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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

    private void processPayment() {
        String address = shippingAddress.getText().toString().trim();
        String emailStr = email.getText().toString().trim();
        String fullNameStr = fullName.getText().toString().trim();
        String phoneNumberStr = phoneNumber.getText().toString().trim();
        String zipCodeStr = zipCode.getText().toString().trim();
        String riderMsgStr = riderMessage.getText().toString().trim();
        String nameOnCard = cardName.getText().toString().trim();
        String cardNum = cardNumber.getText().toString().trim();
        String expiry = expiryDate.getText().toString().trim();
        String cvvCode = cvv.getText().toString().trim();
        String cardType = identifyCardType(cardNum);

        int selectedOption = deliveryOption.getCheckedRadioButtonId();
        String deliveryType = selectedOption == R.id.radio_delivery ? "Delivery" : "Pickup";

        // Validate input fields
        if (emailStr.isEmpty() || fullNameStr.isEmpty() || phoneNumberStr.isEmpty() || zipCodeStr.isEmpty() || nameOnCard.isEmpty() || cardNum.isEmpty() || expiry.isEmpty() || cvvCode.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Process each product as a separate order
        for (CartItem item : cartItems) {
            Map<String, Object> product = new HashMap<>();
            product.put("productId", item.getProductId()); // Store productId
            product.put("productName", item.getProductName());
            product.put("quantity", item.getQuantity());
            product.put("sellerId", item.getSellerId());
            product.put("userId", userId);
            product.put("totalPrice", item.getTotalPrice());
            product.put("imageUrl", item.getImageUrl()); // Pass image URL

            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("cardName", nameOnCard);
            paymentDetails.put("cardNumber", cardNum);
            paymentDetails.put("expiryDate", expiry);
            paymentDetails.put("cvv", cvvCode);
            paymentDetails.put("cardType", cardType);

            // Order details
            Map<String, Object> orderDetails = new HashMap<>();
            orderDetails.put("product", product);
            orderDetails.put("deliveryType", deliveryType);
            orderDetails.put("payment", paymentDetails);
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
            orderDetails.put("status", "Pending");  // Add the status field with a default value

            // Add the order to Firestore for each product
            db.collection("orders").add(orderDetails)
                    .addOnSuccessListener(documentReference -> {
                        deleteCartItems(userId); // Delete cart items after order is placed
                        showCustomDialog(true);
                    })
                    .addOnFailureListener(e -> showCustomDialog(false));
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
