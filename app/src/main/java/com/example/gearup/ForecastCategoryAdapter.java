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
    private final FirebaseFirestore db;

    public ForecastCategoryAdapter(List<ForecastCategoryModel> categoryList) {
        this.categoryList = categoryList;

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

        // Load category image from Firestore
        loadCategoryImage(holder.categoryImage, category.getCategoryTitle());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ForecastDetail.class);
            intent.putExtra("categoryTitle", category.getCategoryTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    private void loadCategoryImage(ImageView imageView, String categoryTitle) {
        CollectionReference categoryImagesRef = db.collection("category_image");

        categoryImagesRef.whereEqualTo("category", categoryTitle).limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String imageUrl = document.getString("image_url");

                        if (imageUrl != null) {
                            Glide.with(imageView.getContext())
                                    .load(imageUrl)
                                    .into(imageView);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Log error or set a default image
                });
    }

    public static class ForecastCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        ImageView categoryImage;

        public ForecastCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            categoryImage = itemView.findViewById(R.id.categoryImage);
        }
    }
}
