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

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final Context context;
    private final List<String> categories;
    private final Map<String, List<Product>> categorizedProducts;
    private final Map<String, Integer> categoryImages;
    private final OnCategoryClickListener onCategoryClickListener;

    public CategoryAdapter(Context context, List<String> categories, Map<String, List<Product>> categorizedProducts,
                           Map<String, Integer> categoryImages, OnCategoryClickListener onCategoryClickListener) {
        this.context = context;
        this.categories = categories;
        this.categorizedProducts = categorizedProducts;
        this.categoryImages = categoryImages;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.categoryName.setText(category);
        holder.categoryItemCount.setText("Items: " + categorizedProducts.get(category).size());

        Glide.with(context).load(categoryImages.get(category)).into(holder.categoryImage);

        holder.itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView categoryItemCount;
        ImageView categoryImage;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            categoryItemCount = itemView.findViewById(R.id.tv_category_count);
            categoryImage = itemView.findViewById(R.id.iv_category_image);
        }
    }
}
