package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrendsFragmentSeller extends Fragment implements PopularProductAdapter.OnItemClickListener {

  /*  private static final String TAG = "TrendsFragmentSeller";
    private ViewPager2 viewPager;
    private PopularProductAdapter adapter;
    private List<PopularProduct> productList = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends_seller, container, false);

        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.view_pager);
        adapter = new PopularProductAdapter(productList, this); // Pass click listener
        viewPager.setAdapter(adapter);

        // Fetch product data
        fetchProducts();

        return view;
    }

    private void fetchProducts() {
        executorService.execute(() -> {
            try {
                List<PopularProduct> fetchedProducts = ProductFetcher.fetchProducts();
                requireActivity().runOnUiThread(() -> {
                    productList.clear();
                    productList.addAll(fetchedProducts);
                    adapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }*/

    @Override
    public void onItemClick(PopularProduct product) {
        Intent intent = new Intent(requireContext(), PopularProductDetail.class);
        intent.putExtra("title", product.getTitle());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("imageUrl", product.getImageUrl());
        intent.putExtra("itemUrl", product.getItemUrl());
        intent.putExtra("condition", product.getCondition());
        intent.putExtra("location", product.getLocation());
        intent.putExtra("shippingCost", product.getShippingCost());
        startActivity(intent);
    }
}