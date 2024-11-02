package com.example.gearup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CartActivity extends AppCompatActivity {
    private Button buttonCart;
    private Button buttonOrdered;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        buttonCart = findViewById(R.id.button_cart);
        buttonOrdered = findViewById(R.id.button_ordered);

        // Set up initial fragment
        if (savedInstanceState == null) {
            loadFragment(new CartFragment());
            highlightButton(buttonCart); // Highlight the cart button initially
        }

        buttonCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new CartFragment());
                highlightButton(buttonCart);
                resetButton(buttonOrdered);
            }
        });

        buttonOrdered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new OrderedProductsFragment());
                highlightButton(buttonOrdered);
                resetButton(buttonCart);
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#FF6200EE")); // Change this color as needed
        button.setTextColor(Color.WHITE);
    }

    private void resetButton(Button button) {
        button.setBackgroundColor(Color.LTGRAY); // Change this color as needed
        button.setTextColor(Color.BLACK);
    }
}
