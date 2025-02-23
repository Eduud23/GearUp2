package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

        // Set user name
        holder.userName.setText(review.getUserName());

        // Set review text
        holder.reviewText.setText(review.getReviewText());

        // Set star rating
        holder.ratingBar.setRating((float)review.getStarRating());

        // Set rating value text
        holder.ratingValue.setText(String.format(Locale.getDefault(), "(%.1f)", review.getStarRating()));

        // Set how long ago the review was made
        holder.reviewDate.setText(getTimeAgo(review.getTimestamp()));

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
        TextView userName, reviewText, reviewDate, ratingValue;
        ImageView profileImage;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            userName = itemView.findViewById(R.id.tv_user_name);
            reviewText = itemView.findViewById(R.id.tv_review_text);
            reviewDate = itemView.findViewById(R.id.tv_review_date);
            ratingValue = itemView.findViewById(R.id.tv_rating_value);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }

    // Function to calculate how long ago a timestamp was
    private String getTimeAgo(Timestamp timestamp) {
        if (timestamp == null) return "Unknown";

        Date reviewDate = timestamp.toDate();
        Date now = new Date();
        long diffMillis = now.getTime() - reviewDate.getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(reviewDate);
        }
    }
}