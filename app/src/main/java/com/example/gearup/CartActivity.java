package com.example.gearup;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart); // Make sure this layout contains a FrameLayout

        // Load the CartFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CartFragment cartFragment = new CartFragment();
        fragmentTransaction.replace(R.id.fragment_container, cartFragment);
        fragmentTransaction.commit();
    }
}
