package com.example.gearup;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductFetcher {
    private static final String TAG = "ProductFetcher";

    public static List<PopularProduct> fetchProducts() {
        List<PopularProduct> products = new ArrayList<>();
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
                products = parseProductData(jsonResponse, exchangeRate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching products", e);
        }
        return products;
    }

    private static double fetchExchangeRate() {
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
        return 56.0; // Default exchange rate
    }

    private static List<PopularProduct> parseProductData(String jsonResponse, double exchangeRate) {
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
}
