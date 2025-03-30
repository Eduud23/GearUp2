package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CheckoutListFormActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCheckout;
    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> checkoutItems;
    private TextView totalAmount;
    private Button confirmCheckoutButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_list_form);

        recyclerViewCheckout = findViewById(R.id.recyclerView_checkout);
        totalAmount = findViewById(R.id.textView_total_checkout);
        confirmCheckoutButton = findViewById(R.id.button_proceed_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewCheckout.setLayoutManager(new LinearLayoutManager(this));

        checkoutItems = getIntent().getParcelableArrayListExtra("cartItems");
        if (checkoutItems == null) {
            checkoutItems = new ArrayList<>();
        }

        checkoutAdapter = new CheckoutAdapter(checkoutItems);
        recyclerViewCheckout.setAdapter(checkoutAdapter);

        updateTotal();

        confirmCheckoutButton.setOnClickListener(v -> goToPaymentActivity());
    }

    private void updateTotal() {
        double total = 0;
        for (CartItem cartItem : checkoutItems) {
            total += cartItem.getTotalPrice();
        }
        totalAmount.setText("Total: ₱" + formatPrice(total));
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    private void goToPaymentActivity() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(checkoutItems));
        intent.putExtra("totalAmount", totalAmount.getText().toString());
        startActivity(intent);
    }
}
