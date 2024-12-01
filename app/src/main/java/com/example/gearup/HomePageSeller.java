package com.example.gearup;

import android.os.Bundle;
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
}
