package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<VideoItem> videoList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(VideoItem video);
    }

    public VideoAdapter(List<VideoItem> videoList, OnItemClickListener onItemClickListener) {
        this.videoList = videoList;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        VideoItem video = videoList.get(position);
        holder.titleTextView.setText(video.getTitle());
        Glide.with(holder.thumbnailImageView.getContext()).load(video.getThumbnail()).into(holder.thumbnailImageView);
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(video));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView thumbnailImageView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.videoTitle);
            thumbnailImageView = itemView.findViewById(R.id.videoThumbnail);
        }
    }
}
