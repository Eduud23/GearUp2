package com.example.gearup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePageSeller extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_seller);

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragmentSeller())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.homeId) {
                selectedFragment = new HomeFragmentSeller();
            }
            else if (itemId == R.id.trendsId) {
                selectedFragment = new TrendsFragmentSeller();
            }
            else if (itemId == R.id.inventoryId) {
                selectedFragment = new InventoryFragment();
            } else if (itemId == R.id.notificationId) {
                selectedFragment = new NotificationFragmentSeller();
            }else if (itemId == R.id.meId) {
                selectedFragment = new MeFragmentSeller();
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
                        Intent intent = new Intent(HomePageSeller.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear the activity stack
                        startActivity(intent);
                        finish(); // Close the current activity
                    }
                })
                .setNegativeButton("No", null) // If user clicks No, just dismiss the dialog
                .show();
    }
}
