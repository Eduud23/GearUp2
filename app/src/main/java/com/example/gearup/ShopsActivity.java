package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ShopsActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ShopsPagerAdapter adapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops);  // Make sure this is your activity layout

        // Initialize views
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        ImageView btnBack = findViewById(R.id.btn_back);
        searchEditText = findViewById(R.id.et_search);

        // Set up adapter
        adapter = new ShopsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Tab labels
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Seller Shops" : "Local Shops");
        }).attach();

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Search listener
        setUpSearchListener();
    }

    private void setUpSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                Fragment current = getCurrentFragment();
                if (current instanceof Searchable) {
                    if (query.isEmpty()) {
                        ((Searchable) current).resetToAllShops();
                    } else {
                        ((Searchable) current).searchShops(query);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private Fragment getCurrentFragment() {
        return adapter.getFragmentAt(viewPager.getCurrentItem());
    }

    public interface Searchable {
        void searchShops(String query);
        void resetToAllShops();
    }
}
