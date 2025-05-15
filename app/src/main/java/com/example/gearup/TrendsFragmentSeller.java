package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class TrendsFragmentSeller extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TrendsPagerAdapter pagerAdapter;
    private EditText etSearch;

    private LocalTrendsFragment localTrendsFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends_seller, container, false);

        etSearch = view.findViewById(R.id.et_search);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        View forecastButton = view.findViewById(R.id.btn_see_forecast);
        forecastButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Forecast.class);
            startActivity(intent);
        });

        List<Fragment> fragments = new ArrayList<>();

        localTrendsFragment = new LocalTrendsFragment();
        fragments.add(localTrendsFragment);

        pagerAdapter = new TrendsPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText("Philippines");
        }).attach();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String searchQuery = charSequence.toString();
                localTrendsFragment.setSearchQuery(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }
}
