package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class CheckoutFormActivity extends AppCompatActivity {

    private TextView productName, productBrand, productYear, productPrice, productQuantity;
    private EditText shippingAddress, cardName, cardNumber, expiryDate, cvv, riderMessage;
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
        cardName = findViewById(R.id.card_name);
        cardNumber = findViewById(R.id.card_number);
        expiryDate = findViewById(R.id.expiry_date);
        cvv = findViewById(R.id.cvv);
        riderMessage = findViewById(R.id.rider_message);
        deliveryOption = findViewById(R.id.delivery_option);
        confirmPayment = findViewById(R.id.confirm_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

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

        confirmPayment.setText("Confirm Payment ( ₱" + finalPrice + ")");
        confirmPayment.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        String address = shippingAddress.getText().toString().trim();
        String nameOnCard = cardName.getText().toString().trim();
        String cardNum = cardNumber.getText().toString().trim();
        String expiry = expiryDate.getText().toString().trim();
        String cvvCode = cvv.getText().toString().trim();
        String message = riderMessage.getText().toString().trim();

        int selectedOption = deliveryOption.getCheckedRadioButtonId();
        String deliveryType = selectedOption == R.id.radio_delivery ? "Delivery" : "Pickup";

        if (cardNum.isEmpty() || expiry.isEmpty() || cvvCode.isEmpty() || nameOnCard.isEmpty() || (selectedOption == R.id.radio_delivery && address.isEmpty())) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Payment Successful! Order Confirmed.", Toast.LENGTH_LONG).show();
        finish();
    }
}
