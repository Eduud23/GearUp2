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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

    public class TrendsFragment extends Fragment implements PopularProductAdapter.OnItemClickListener {

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
        adapter = new PopularProductAdapter(productList, this); // Pass click listener
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
        return 56.0;
    }

        private List<PopularProduct> parseProductData(String jsonResponse, double exchangeRate) {
            List<PopularProduct> products = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray findItemsArray = jsonObject.getJSONArray("findItemsByKeywordsResponse");

                if (findItemsArray.length() > 0) {
                    JSONArray searchResultArray = findItemsArray.getJSONObject(0).optJSONArray("searchResult");

                    if (searchResultArray != null && searchResultArray.length() > 0) {
                        JSONArray items = searchResultArray.getJSONObject(0).optJSONArray("item");

                        if (items != null) {
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject item = items.getJSONObject(i);
                                String title = item.optJSONArray("title") != null ?
                                        item.getJSONArray("title").optString(0, "No Title") : "No Title";

                                // Price
                                double usdPrice = 0.00;
                                JSONArray sellingStatusArray = item.optJSONArray("sellingStatus");
                                if (sellingStatusArray != null && sellingStatusArray.length() > 0) {
                                    JSONObject sellingStatus = sellingStatusArray.getJSONObject(0);
                                    JSONArray currentPriceArray = sellingStatus.optJSONArray("currentPrice");
                                    if (currentPriceArray != null && currentPriceArray.length() > 0) {
                                        usdPrice = currentPriceArray.getJSONObject(0).optDouble("__value__", 0.00);
                                    }
                                }
                                String price = "₱" + String.format("%.2f", usdPrice * exchangeRate);

                                // Image URL
                                String imageUrl = item.optJSONArray("galleryURL") != null ?
                                        item.getJSONArray("galleryURL").optString(0, "") : "";

                                // Product URL
                                String itemUrl = item.optJSONArray("viewItemURL") != null ?
                                        item.getJSONArray("viewItemURL").optString(0, "") : "";

                                // Condition
                                String condition = "Unknown";
                                JSONArray conditionArray = item.optJSONArray("condition");
                                if (conditionArray != null && conditionArray.length() > 0) {
                                    condition = conditionArray.getJSONObject(0).optString("conditionDisplayName", "Unknown");
                                }

                                // Location
                                String location = "Not Specified";
                                JSONArray locationArray = item.optJSONArray("location");
                                if (locationArray != null && locationArray.length() > 0) {
                                    location = locationArray.optString(0, "Not Specified");
                                }

                                // Shipping Cost
                                String shippingCost = "Varies";
                                JSONArray shippingArray = item.optJSONArray("shippingInfo");
                                if (shippingArray != null && shippingArray.length() > 0) {
                                    JSONObject shippingInfo = shippingArray.getJSONObject(0);
                                    JSONArray shippingServiceCost = shippingInfo.optJSONArray("shippingServiceCost");
                                    if (shippingServiceCost != null && shippingServiceCost.length() > 0) {
                                        double cost = shippingServiceCost.getJSONObject(0).optDouble("__value__", -1);
                                        if (cost >= 0) {
                                            shippingCost = "₱" + String.format("%.2f", cost * exchangeRate);
                                        } else {
                                            shippingCost = "Free Shipping";
                                        }
                                    }
                                }


                                // Add product to list
                                products.add(new PopularProduct(title, price, imageUrl, itemUrl, condition, location, shippingCost));
                            }
                        }
                    }
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
