package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

public class ShopsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ShopsPagerAdapter adapter;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shops, container, false);

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        ImageView btnBack = view.findViewById(R.id.btn_back);
        searchEditText = view.findViewById(R.id.et_search);

        // Initialize the adapter
        adapter = new ShopsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Set up TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Seller Shops");
            } else {
                tab.setText("Local Shops");
            }
        }).attach();

        // Handle back button click
        btnBack.setOnClickListener(v -> getActivity().onBackPressed());

        // Set up search functionality
        setUpSearchListener();

        return view;
    }

    private void setUpSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString().trim();
                Log.d(TAG, "Search query changed: " + query);

                // Get the current fragment and handle search or reset
                Fragment currentFragment = getCurrentFragment();
                if (currentFragment instanceof Searchable) {
                    if (query.isEmpty()) {
                        Log.d(TAG, "Search query is empty, resetting to all items.");
                        ((Searchable) currentFragment).resetToAllShops();  // Reset to all items
                    } else {
                        Log.d(TAG, "Calling searchShops with query: " + query);
                        ((Searchable) currentFragment).searchShops(query);  // Perform the search
                    }
                } else {
                    Log.d(TAG, "Current fragment does not implement Searchable");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // Get the current fragment based on ViewPager2's selected tab
    private Fragment getCurrentFragment() {
        int position = viewPager.getCurrentItem();
        return getChildFragmentManager().findFragmentByTag("f" + position);
    }

    // Searchable interface to ensure fragments have a search method
    public interface Searchable {
        void searchShops(String query);

        // Method to reset to the original list when the search is cleared
        void resetToAllShops();
    }
}
