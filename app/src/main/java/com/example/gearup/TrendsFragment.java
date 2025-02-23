package com.example.gearup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrendsFragment extends Fragment {

    private static final String TAG = "TrendsFragment";
    private ViewPager2 viewPager;
    private PopularProductAdapter adapter;
    private List<PopularProduct> productList = new ArrayList<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends_buyer, container, false);

        // Initialize ViewPager2
        viewPager = view.findViewById(R.id.view_pager);
        adapter = new PopularProductAdapter(productList);
        viewPager.setAdapter(adapter);

        // Fetch product data
        fetchProducts();

        return view;
    }

    private void fetchProducts() {
        executorService.execute(() -> {
            try {
                String urlString = "https://svcs.ebay.com/services/search/FindingService/v1?" +
                        "OPERATION-NAME=findItemsByKeywords&" +
                        "SERVICE-VERSION=1.13.0&" +
                        "SECURITY-APPNAME=RoronoaZ-products-PRD-1dec4f011-15ef659f&" +
                        "RESPONSE-DATA-FORMAT=JSON&" +
                        "keywords=popular+Auto+Parts+%26+Accessories";
                String jsonResponse = NetworkUtils.fetchData(urlString);

                if (jsonResponse != null) {
                    double exchangeRate = fetchExchangeRate();
                    List<PopularProduct> fetchedProducts = parseProductData(jsonResponse, exchangeRate);
                    requireActivity().runOnUiThread(() -> {
                        productList.clear();
                        productList.addAll(fetchedProducts);
                        adapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products", e);
            }
        });
    }

    private double fetchExchangeRate() {
        try {
            String url = "https://api.exchangerate-api.com/v4/latest/USD";
            String response = NetworkUtils.fetchData(url);
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                return jsonObject.getJSONObject("rates").getDouble("PHP");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching exchange rate", e);
        }
        return 56.0; // Fallback value if API fails
    }

    private List<PopularProduct> parseProductData(String jsonResponse, double exchangeRate) {
        List<PopularProduct> products = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.getJSONArray("findItemsByKeywordsResponse")
                    .getJSONObject(0)
                    .getJSONArray("searchResult")
                    .getJSONObject(0)
                    .getJSONArray("item");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String title = item.optJSONArray("title") != null ? item.getJSONArray("title").optString(0, "No Title") : "No Title";

                double usdPrice = item.optJSONArray("sellingStatus") != null &&
                        item.getJSONArray("sellingStatus").optJSONObject(0) != null &&
                        item.getJSONArray("sellingStatus").optJSONObject(0).optJSONArray("currentPrice") != null ?
                        item.getJSONArray("sellingStatus").optJSONObject(0)
                                .optJSONArray("currentPrice").optJSONObject(0)
                                .optDouble("__value__", 0.00) : 0.00;

                String price = "₱" + String.format("%.2f", usdPrice * exchangeRate);
                String imageUrl = item.optJSONArray("galleryURL") != null ? item.getJSONArray("galleryURL").optString(0, "") : "";
                String itemUrl = item.optJSONArray("viewItemURL") != null ? item.getJSONArray("viewItemURL").optString(0, "") : "";

                products.add(new PopularProduct(title, price, imageUrl, itemUrl));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
        return products;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
