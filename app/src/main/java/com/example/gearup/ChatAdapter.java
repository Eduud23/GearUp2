package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages;
    private String currentUserId;
    private FirebaseFirestore db;

    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_sender_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_message_receiver_item, parent, false);
        }
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);

        holder.messageTextView.setText(message.getContent());
        holder.timestampTextView.setText(formatTimestamp(message.getTimestamp()));
        loadProfileImage(message.getSenderId(), holder.profileImageView);
        loadUserName(message.getSenderId(), holder.nameTextView);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(currentUserId)) {
            return 1;
        } else {
            return 0;
        }
    }

    private void loadProfileImage(String userId, final ImageView imageView) {
        String collection = userId.equals(currentUserId) ? "buyers" : "sellers";
        db.collection(collection).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        Glide.with(imageView.getContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.gear)
                                .into(imageView);
                    }
                });
    }

    private void loadUserName(String userId, final TextView nameTextView) {
        String collection = userId.equals(currentUserId) ? "buyers" : "sellers";
        db.collection(collection).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = userId.equals(currentUserId)
                                ? documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName")
                                : documentSnapshot.getString("shopName");
                        nameTextView.setText(name != null ? name : "Unknown");
                    }
                });
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;
        ImageView profileImageView;
        TextView nameTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            timestampTextView = itemView.findViewById(R.id.tv_timestamp);
            profileImageView = itemView.findViewById(R.id.profile_image);
            nameTextView = itemView.findViewById(R.id.tv_name);
        }
    }
}
