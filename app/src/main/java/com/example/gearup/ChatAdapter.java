package com.example.gearup;

import android.util.Log;
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

        // Load profile image and name for the sender
        loadSenderInfo(message.getSenderId(), holder.profileImageView, holder.senderNameTextView);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSenderId().equals(currentUserId)) {
            return 1; // Sender
        } else {
            return 0; // Receiver
        }
    }

    // Load profile image and sender's name based on userId
    private void loadSenderInfo(String userId, final ImageView imageView, final TextView senderNameTextView) {
        // We will check both the 'buyers' and 'sellers' collections
        // and fetch the profile image and name accordingly.
        checkSenderInfo(userId, "buyers", imageView, senderNameTextView);
        checkSenderInfo(userId, "sellers", imageView, senderNameTextView);
    }

    // Check if profile image and sender's name exist in a specific collection
    private void checkSenderInfo(String userId, String collection, final ImageView imageView, final TextView senderNameTextView) {
        db.collection(collection).document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (collection.equals("buyers")) {
                            // For buyers, get the first and last name
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            senderNameTextView.setText(firstName + " " + lastName);
                        } else if (collection.equals("sellers")) {
                            // For sellers, get the shop name
                            String shopName = documentSnapshot.getString("shopName");
                            senderNameTextView.setText(shopName != null ? shopName : "Shop");
                        }

                        // Load the profile image if available
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null) {
                            Log.d("ChatAdapter", "Profile image URL for userId: " + userId + " from collection: " + collection + " -> " + profileImageUrl);
                            Glide.with(imageView.getContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.gear)
                                    .into(imageView);
                        }
                    } else {
                        Log.d("ChatAdapter", "No profile found for userId: " + userId + " in collection: " + collection);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatAdapter", "Error fetching profile image for userId: " + userId + " from collection: " + collection, e);
                });
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;
        TextView senderNameTextView;  // Added for sender's name
        ImageView profileImageView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
            timestampTextView = itemView.findViewById(R.id.tv_timestamp);
            senderNameTextView = itemView.findViewById(R.id.tv_name);  // Initialize sender's name TextView
            profileImageView = itemView.findViewById(R.id.profile_image);
        }
    }
}
