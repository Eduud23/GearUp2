package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class TrendsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TrendsPagerAdapter pagerAdapter;
    private EditText etSearch;

    // Keep references to the fragments so we can access them later
    private InternationalTrendsFragment internationalTrendsFragment;
    private LocalTrendsFragment localTrendsFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends, container, false);

        etSearch = view.findViewById(R.id.et_search);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        List<Fragment> fragments = new ArrayList<>();

        internationalTrendsFragment = new InternationalTrendsFragment();
        localTrendsFragment = new LocalTrendsFragment();
        SocialMediaTrendsFragment socialMediaTrendsFragment = new SocialMediaTrendsFragment();

        // Default search argument for international
        Bundle searchBundle = new Bundle();
        searchBundle.putString("search_query", "");
        internationalTrendsFragment.setArguments(searchBundle);

        fragments.add(internationalTrendsFragment);
        fragments.add(localTrendsFragment);
        fragments.add(socialMediaTrendsFragment);

        pagerAdapter = new TrendsPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("International");
                    break;
                case 1:
                    tab.setText("Philippines");
                    break;
                case 2:
                    tab.setText("Social Media");
                    break;
            }
        }).attach();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String searchQuery = charSequence.toString();
                internationalTrendsFragment.setSearchQuery(searchQuery);
                localTrendsFragment.setSearchQuery(searchQuery); // 🔍 added line
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }
}
