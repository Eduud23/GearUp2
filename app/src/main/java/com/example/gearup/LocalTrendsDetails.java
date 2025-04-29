package com.example.gearup;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalTrendsDetails extends AppCompatActivity {

    private RecyclerView similarProductsRecyclerView;
    private LocalTrendsAdapter similarProductsAdapter;
    private List<LocalTrendsData> similarProductsList = new ArrayList<>();
    private TextView seeAllTextView;  // Add the See All TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_local_trends_details);

        ImageView imageView = findViewById(R.id.product_image);
        TextView nameTextView = findViewById(R.id.product_name);
        TextView placeTextView = findViewById(R.id.product_place);
        TextView priceTextView = findViewById(R.id.product_price);
        TextView ratingsTextView = findViewById(R.id.product_ratings);
        TextView soldTextView = findViewById(R.id.product_sold);
        TextView promoTextView = findViewById(R.id.product_promo);
        Button openLinkButton = findViewById(R.id.open_link_button);
        similarProductsRecyclerView = findViewById(R.id.similarProductsRecyclerView);
        seeAllTextView = findViewById(R.id.seeAllTextView);  // Initialize See All TextView

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize RecyclerView for similar products
        similarProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        similarProductsAdapter = new LocalTrendsAdapter(similarProductsList, data -> {
            // Handle click on similar products (open their details)
            Intent intent = new Intent(LocalTrendsDetails.this, LocalTrendsDetails.class);
            intent.putExtra("image", data.getImage());
            intent.putExtra("name", data.getName());
            intent.putExtra("place", data.getPlace());
            intent.putExtra("price", data.getPrice());
            intent.putExtra("ratings", data.getRatings());
            intent.putExtra("sold", data.getSold());
            intent.putExtra("promo", data.getPromo());
            intent.putExtra("link", data.getLink());
            startActivity(intent);
        });
        similarProductsRecyclerView.setAdapter(similarProductsAdapter);

        // Get data from intent for selected product
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("image");
        String name = intent.getStringExtra("name");
        String place = intent.getStringExtra("place");
        double price = intent.getDoubleExtra("price", 0.0);
        double ratings = intent.getDoubleExtra("ratings", 0.0);
        String sold = intent.getStringExtra("sold");
        String promo = intent.getStringExtra("promo");
        String link = intent.getStringExtra("link");

        // Set data to views
        Glide.with(this).load(imageUrl).into(imageView);
        nameTextView.setText(name);
        placeTextView.setText("Location: " + place);
        priceTextView.setText("Price: ₱" + price);
        ratingsTextView.setText("Ratings: " + ratings);
        soldTextView.setText("Sold: " + sold);
        promoTextView.setText("Promo: " + (promo.isEmpty() ? "No Promo" : promo));

        // Open product link in browser
        openLinkButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        });

        // Fetch similar products from Firestore
        fetchSimilarProductsData();
    }

    private void fetchSimilarProductsData() {
        // Initialize Firestore with the custom FirebaseApp instance
        try {
            FirebaseApp thirdApp = FirebaseApp.getInstance("gearupdataThirdApp");
            FirebaseFirestore db = FirebaseFirestore.getInstance(thirdApp); // Use the custom FirebaseApp

            Log.d(TAG, "✅ Connected to Firestore (gearupdataThirdApp)");

            // Fetch similar products without a limit
            db.collection("shopee_products")
                    .get()  // No limit to the number of products
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            similarProductsList.clear();  // Clear the current list of similar products
                            List<LocalTrendsData> fetchedProducts = new ArrayList<>();

                            QuerySnapshot result = task.getResult();
                            if (result != null) {
                                for (QueryDocumentSnapshot document : result) {
                                    // Create a new product object
                                    LocalTrendsData data = new LocalTrendsData();
                                    data.setImage(document.getString("image"));
                                    data.setLink(document.getString("link"));
                                    data.setName(document.getString("name"));
                                    data.setPlace(document.getString("place"));

                                    // Handle price
                                    Object priceObj = document.get("price");
                                    if (priceObj instanceof Number) {
                                        data.setPrice(((Number) priceObj).doubleValue());
                                    } else if (priceObj instanceof String) {
                                        try {
                                            data.setPrice(Double.parseDouble((String) priceObj));
                                        } catch (NumberFormatException e) {
                                            data.setPrice(0.0);
                                        }
                                    } else {
                                        data.setPrice(0.0);
                                    }

                                    // Handle ratings
                                    Object ratingsObj = document.get("ratings");
                                    if (ratingsObj instanceof Number) {
                                        data.setRatings(((Number) ratingsObj).doubleValue());
                                    } else if (ratingsObj instanceof String) {
                                        try {
                                            data.setRatings(Double.parseDouble((String) ratingsObj));
                                        } catch (NumberFormatException e) {
                                            data.setRatings(0.0);
                                        }
                                    } else {
                                        data.setRatings(0.0);
                                    }

                                    // Handle promo
                                    Object promoObj = document.get("promo");
                                    if (promoObj instanceof String) {
                                        data.setPromo((String) promoObj);
                                    } else if (promoObj != null) {
                                        data.setPromo(promoObj.toString());
                                    } else {
                                        data.setPromo("");
                                    }

                                    // Handle sale
                                    Object saleObj = document.get("sale");
                                    if (saleObj instanceof Number) {
                                        data.setSale(((Number) saleObj).intValue());
                                    } else if (saleObj instanceof String) {
                                        try {
                                            data.setSale(Integer.parseInt((String) saleObj));
                                        } catch (NumberFormatException e) {
                                            data.setSale(0);
                                        }
                                    } else {
                                        data.setSale(0);
                                    }

                                    // Handle sold count
                                    data.setSold(document.getString("sold") != null ? document.getString("sold") : "0");

                                    // Add the product to the list
                                    fetchedProducts.add(data);
                                }

                                // Shuffle the products (optional)
                                Collections.shuffle(fetchedProducts);

                                // Log the number of products fetched
                                Log.d(TAG, "Fetched " + fetchedProducts.size() + " similar products");

                                // Update RecyclerView with similar products
                                if (fetchedProducts.isEmpty()) {
                                    Log.d(TAG, "No similar products found.");
                                } else {
                                    // Update the adapter with the new list of products
                                    similarProductsAdapter.updateProducts(fetchedProducts);
                                    Log.d(TAG, "Similar products updated in RecyclerView.");

                                    // Show "See All" if there are more than 4 products
                                    if (fetchedProducts.size() > 4) {
                                        seeAllTextView.setVisibility(View.VISIBLE);
                                        seeAllTextView.setOnClickListener(v -> {
                                            // Show all products when "See All" is clicked
                                            Intent intent = new Intent(LocalTrendsDetails.this, AllSimilarLocalProducts.class);
                                            intent.putParcelableArrayListExtra("allProducts", new ArrayList<>(fetchedProducts));
                                            startActivity(intent);
                                        });
                                    }
                                }

                            } else {
                                Log.e(TAG, "No products found in Firestore.");
                            }
                        } else {
                            Log.e(TAG, "❌ Error fetching similar products", task.getException());
                        }
                    });

        } catch (IllegalStateException e) {
            Log.e(TAG, "❌ FirebaseApp 'gearupdataThirdApp' not found.", e);
        }
    }
}
