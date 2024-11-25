package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;

    // Constructor
    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout for each notification
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Set message and timestamp data
        holder.messageTextView.setText(notification.getMessage());
        holder.timestampTextView.setText(notification.getFormattedTimestamp());  // Use the formatted timestamp
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;  // Add reference for timestamp TextView

        public NotificationViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);  // Adjust according to your layout
            timestampTextView = itemView.findViewById(R.id.timestampTextView);  // Adjust according to your layout
        }
    }
}
