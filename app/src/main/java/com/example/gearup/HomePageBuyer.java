package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePageBuyer extends AppCompatActivity {
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_buyer);

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragmentBuyer())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.homeBuyerId) {
                selectedFragment = new HomeFragmentBuyer();
            } else if (itemId == R.id.trendsId) {
                selectedFragment = new TrendsFragment();
            } else if (itemId == R.id.notificationId) {
                selectedFragment = new NotificationFragmentBuyer();
            } else if (itemId == R.id.meId) {
                selectedFragment = new MeFragmentBuyer();
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Show a confirmation dialog when the back button is pressed
        new AlertDialog.Builder(this)
                .setMessage("Do you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Navigate to the Login screen
                        Intent intent = new Intent(HomePageBuyer.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
                        startActivity(intent);
                        finish(); // Close the current activity
                    }
                })
                .setNegativeButton("No", null) // If user clicks No, just dismiss the dialog
                .show();
    }
}
