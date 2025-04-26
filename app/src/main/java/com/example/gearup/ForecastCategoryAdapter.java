package com.example.gearup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseApp;

import java.util.List;

public class ForecastCategoryAdapter extends RecyclerView.Adapter<ForecastCategoryAdapter.ForecastCategoryViewHolder> {

    private final List<ForecastCategoryModel> categoryList;
    private final String productTitle;
    private final FirebaseFirestore db;

    public ForecastCategoryAdapter(List<ForecastCategoryModel> categoryList, String productTitle) {
        this.categoryList = categoryList;
        this.productTitle = productTitle;

        // Initialize Firebase with the specific instance ("gearupdataFifthApp")
        FirebaseApp gearupdataFifthApp = FirebaseApp.getInstance("gearupdataFifthApp");
        this.db = FirebaseFirestore.getInstance(gearupdataFifthApp);
    }

    @NonNull
    @Override
    public ForecastCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast_category, parent, false);
        return new ForecastCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastCategoryViewHolder holder, int position) {
        ForecastCategoryModel category = categoryList.get(position);
        holder.categoryTitle.setText(category.getCategoryTitle());
        holder.forecastedQuantity.setText("Forecasted Quantity: " + category.getForecastedQuantity() + " units");

        // Fetch and display category image
        loadCategoryImage(holder.categoryImage, category.getCategoryTitle());

        // Set click listener to go to ForecastDetail activity
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ForecastDetail.class);
            intent.putExtra("categoryTitle", category.getCategoryTitle());
            intent.putExtra("productTitle", productTitle); // pass the product title
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    private void loadCategoryImage(ImageView imageView, String categoryTitle) {
        // Reference to the category image collection in Firestore
        CollectionReference categoryImagesRef = db.collection("category_image");

        // Query for category image URL where the category name matches the categoryTitle
        categoryImagesRef.whereEqualTo("category", categoryTitle).limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String imageUrl = document.getString("image_url");

                        if (imageUrl != null) {
                            // Use Glide to load the image into the ImageView
                            Glide.with(imageView.getContext())
                                    .load(imageUrl)
                                    .into(imageView);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error (you can add a default image or log the error)
                });
    }

    public static class ForecastCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        TextView forecastedQuantity;
        ImageView categoryImage;

        public ForecastCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            forecastedQuantity = itemView.findViewById(R.id.forecastedQuantity);
            categoryImage = itemView.findViewById(R.id.categoryImage); // Assuming this is in your item layout
        }
    }
}
