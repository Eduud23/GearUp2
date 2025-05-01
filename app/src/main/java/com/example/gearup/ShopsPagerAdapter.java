package com.example.gearup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ShopsPagerAdapter extends FragmentStateAdapter {

    private final Fragment[] fragments;

    public ShopsPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
        fragments = new Fragment[] {
                new SellerShopsFragment(),
                new LocalShopsFragment()
        };
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public Fragment getFragmentAt(int position) {
        return fragments[position];
    }
}
