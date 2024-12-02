package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Set review text
        holder.reviewText.setText(review.getReviewText());

        // Set the user name
        holder.userName.setText(review.getUserName());

        // Load the user's profile image using Glide
        if (review.getProfileImageUrl() != null && !review.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(review.getProfileImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)  // Placeholder image
                    .into(holder.profileImage);
        } else {
            // Set a default profile image if not available
            holder.profileImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, reviewText;
        ImageView profileImage;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            userName = itemView.findViewById(R.id.tv_user_name);
            reviewText = itemView.findViewById(R.id.tv_review_text);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
        }
    }
}
