package com.example.gearup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<String> categories;
    private Map<String, List<Product>> categorizedProducts;
    private Map<String, String> categoryImages; // Change to String for URL
    private Map<String, Integer> categoryCounts; // For counts
    private OnCategoryClickListener onCategoryClickListener;

    public CategoryAdapter(Context context, List<String> categories,
                           Map<String, List<Product>> categorizedProducts,
                           Map<String, String> categoryImages, // Change to String for URL
                           Map<String, Integer> categoryCounts, // For counts
                           OnCategoryClickListener onCategoryClickListener) {
        this.context = context;
        this.categories = categories;
        this.categorizedProducts = categorizedProducts;
        this.categoryImages = categoryImages; // Initialize with URL map
        this.categoryCounts = categoryCounts; // Initialize counts
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryName.setText(category);
        holder.itemCount.setText(String.valueOf(categoryCounts.get(category))); // Show count

        // Load the category logo from the URL
        Glide.with(context)
                .load(categoryImages.get(category))
                .into(holder.categoryImage);

        holder.itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, itemCount;
        ImageView categoryImage;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            itemCount = itemView.findViewById(R.id.tv_item_count); // Display count here
            categoryImage = itemView.findViewById(R.id.iv_category_image);
        }
    }
}
