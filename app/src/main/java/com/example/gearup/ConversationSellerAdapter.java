package com.example.gearup;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ConversationSellerAdapter extends RecyclerView.Adapter<ConversationSellerAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private String currentUserId;

    public ConversationSellerAdapter(List<Conversation> conversations, String currentUserId) {
        this.conversations = conversations;
        this.currentUserId = currentUserId;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item_conversation layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        // Get the current conversation
        Conversation conversation = conversations.get(position);

        // Set the buyer's full name as the conversation name (for the seller)
        holder.conversationNameTextView.setText(conversation.getShopName());

        // Set the last message in the conversation
        holder.lastMessageTextView.setText(conversation.getLastMessage());

        // Load the buyer's profile image using Glide
        String profileImageUrl = conversation.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            // Load the profile image from the URL using Glide
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .circleCrop() // Optional: makes the image circular
                    .into(holder.profileImageView);
        } else {
            // Set a default profile image if no URL is provided
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.gear) // Placeholder or default image
                    .circleCrop()
                    .into(holder.profileImageView);
        }

        // Set click listener for each conversation item
        holder.itemView.setOnClickListener(v -> {
            // Get the buyerId and sellerId for the conversation
            String buyerId = getBuyerId(conversation);
            String sellerId = currentUserId; // The current user is the seller

            // Prepare the Intent to navigate to the ChatActivity
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("CHATROOM_ID", conversation.getId()); // Pass the chatroom ID
            intent.putExtra("BUYER_ID", buyerId); // Pass the buyer's ID
            intent.putExtra("SELLER_ID", sellerId); // Pass the seller's ID
            intent.putExtra("CURRENT_USER_ID", currentUserId); // Pass the current user ID (seller)

            // Start the ChatActivity
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        TextView conversationNameTextView;
        TextView lastMessageTextView;
        ImageView profileImageView; // Added ImageView for the profile picture

        public ConversationViewHolder(View itemView) {
            super(itemView);
            conversationNameTextView = itemView.findViewById(R.id.tv_conversation_name);
            lastMessageTextView = itemView.findViewById(R.id.tv_last_message);
            profileImageView = itemView.findViewById(R.id.profile_image); // Initialize ImageView
        }
    }

    // Helper method to get the buyer's ID (from the conversation's participants list)
    private String getBuyerId(Conversation conversation) {
        List<String> participants = conversation.getParticipants();
        for (String participant : participants) {
            if (!participant.equals(currentUserId)) { // Ensure we don't return the current user's ID
                return participant; // The other participant is the buyer
            }
        }
        return null;
    }

    // Method to update the list of conversations and notify the adapter
    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }
}
