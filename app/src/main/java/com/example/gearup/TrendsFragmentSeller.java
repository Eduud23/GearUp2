package com.example.gearup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends_seller, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new InternationalTrendsFragment()); // International
        fragments.add(new LocalTrendsFragment());        // Local
        fragments.add(new SocialMediaTrendsFragment());  // Social Media

        pagerAdapter = new TrendsPagerAdapter(this, fragments);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("International");
                    break;
                case 1:
                    tab.setText("Local");
                    break;
                case 2:
                    tab.setText("Social Media");
                    break;
            }
        }).attach();

        return view;
    }
}
