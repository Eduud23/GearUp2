package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    private List<Product> productList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onRecommendedProductClick(Product product);
    }

    public RecommendationAdapter(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setProductList(List<Product> products) {
        this.productList.clear();
        this.productList.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int firstIndex = position * 2;
        int secondIndex = firstIndex + 1;

        // Bind First Product
        if (firstIndex < productList.size()) {
            bindProduct(holder, 0, productList.get(firstIndex));
        } else {
            hideProductViews(holder, 0);
        }

        // Bind Second Product (if exists)
        if (secondIndex < productList.size()) {
            bindProduct(holder, 1, productList.get(secondIndex));
        } else {
            hideProductViews(holder, 1);
        }
    }

    private void bindProduct(ViewHolder holder, int index, Product product) {
        holder.productName[index].setText(product.getName());
        holder.productPrice[index].setText("Price: "+ String.format("₱%,.2f", product.getPrice()));
        holder.productDescription[index].setText("Description "+product.getDescription());

        // Load Product Image
        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(product.getImageUrls().get(0)).into(holder.productImage[index]);
        } else {
            holder.productImage[index].setImageResource(R.drawable.gear);
        }

        // Load Seller Profile Image
        if (product.getSellerProfileImageUrl() != null && !product.getSellerProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(product.getSellerProfileImageUrl()).into(holder.sellerImage[index]);
        } else {
            holder.sellerImage[index].setImageResource(R.drawable.gear);
        }

        // Set click listener
        holder.productImage[index].setOnClickListener(v -> onItemClickListener.onRecommendedProductClick(product));

        // Ensure Views Are Visible
        holder.productName[index].setVisibility(View.VISIBLE);
        holder.productPrice[index].setVisibility(View.VISIBLE);
        holder.productDescription[index].setVisibility(View.VISIBLE);
        holder.productImage[index].setVisibility(View.VISIBLE);
        holder.sellerImage[index].setVisibility(View.VISIBLE);
    }

    private void hideProductViews(ViewHolder holder, int index) {
        holder.productName[index].setVisibility(View.GONE);
        holder.productPrice[index].setVisibility(View.GONE);
        holder.productDescription[index].setVisibility(View.GONE);
        holder.productImage[index].setVisibility(View.GONE);
        holder.sellerImage[index].setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return (productList.size() + 1) / 2; // Two products per row
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView[] productName = new TextView[2];
        TextView[] productPrice = new TextView[2];
        TextView[] productDescription = new TextView[2];
        ImageView[] productImage = new ImageView[2];
        ImageView[] sellerImage = new ImageView[2];

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName[0] = itemView.findViewById(R.id.tv_product_name);
            productPrice[0] = itemView.findViewById(R.id.tv_product_price);
            productDescription[0] = itemView.findViewById(R.id.tv_product_description);
            productImage[0] = itemView.findViewById(R.id.iv_product_image);
            sellerImage[0] = itemView.findViewById(R.id.civ_seller_profile_image);

            productName[1] = itemView.findViewById(R.id.tv_product_name_2);
            productPrice[1] = itemView.findViewById(R.id.tv_product_price_2);
            productDescription[1] = itemView.findViewById(R.id.tv_product_description_2);
            productImage[1] = itemView.findViewById(R.id.iv_product_image_2);
            sellerImage[1] = itemView.findViewById(R.id.civ_seller_profile_image_2);
        }
    }
}
