package com.example.gearup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ShopsPagerAdapter extends FragmentStateAdapter {

    public ShopsPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new SellerShopsFragment();
        } else {
            return new LocalShopsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}