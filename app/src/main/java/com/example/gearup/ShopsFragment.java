package com.example.gearup;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ShopsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ShopsPagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shops, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        ImageView btnBack = view.findViewById(R.id.btn_back);



        adapter = new ShopsPagerAdapter(this);
        viewPager.setAdapter(adapter);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Seller Shops");
            } else {
                tab.setText("Local Shops");
            }
        }).attach();

        return view;
    }
}