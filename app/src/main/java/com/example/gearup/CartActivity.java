package com.example.gearup;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CartActivity extends AppCompatActivity {
    private Button buttonCart;
    private Button buttonOrdered;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        buttonCart = findViewById(R.id.button_cart);
        buttonOrdered = findViewById(R.id.button_ordered);
        constraintLayout = findViewById(R.id.constraintLayout);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });


        // Set up initial fragment
        if (savedInstanceState == null) {
            loadFragment(new CartFragment());
            highlightButton(buttonCart); // Highlight the cart button initially
            adjustButtonSizes("cart");
        }

        buttonCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new CartFragment());
                highlightButton(buttonCart);
                resetButton(buttonOrdered);
                adjustButtonSizes("cart");
            }
        });

        buttonOrdered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new OrderedProductsFragment());
                highlightButton(buttonOrdered);
                resetButton(buttonCart);
                adjustButtonSizes("ordered");
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
        button.setBackgroundColor(Color.parseColor("#FF6200EE")); // Highlight color
        button.setTextColor(Color.WHITE);
    }

    private void resetButton(Button button) {
        button.setBackgroundColor(Color.LTGRAY); // Default button color
        button.setTextColor(Color.BLACK);
    }

    private void adjustButtonSizes(String clickedButton) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        if ("cart".equals(clickedButton)) {
            // Cart button larger
            constraintSet.constrainPercentWidth(R.id.button_cart, 0.6f);
            constraintSet.constrainPercentWidth(R.id.button_ordered, 0.3f);
        } else {
            // Ordered button larger
            constraintSet.constrainPercentWidth(R.id.button_cart, 0.3f);
            constraintSet.constrainPercentWidth(R.id.button_ordered, 0.6f);
        }

        constraintSet.applyTo(constraintLayout);
    }
}

