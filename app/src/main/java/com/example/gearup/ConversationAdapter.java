package com.example.gearup;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Add Glide for image loading

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private String currentUserId; // Add currentUserId as a field

    // Constructor now accepts both conversations and currentUserId
    public ConversationAdapter(List<Conversation> conversations, String currentUserId) {
        this.conversations = conversations;
        this.currentUserId = currentUserId; // Initialize the currentUserId
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

        // Set the conversation name, use shopName for sellers or buyer's full name
        String conversationName = conversation.getShopName() != null ? conversation.getShopName() : conversation.getName();
        holder.conversationNameTextView.setText(conversationName); // Set the name for conversation

        // Set the last message in the conversation
        holder.lastMessageTextView.setText(conversation.getLastMessage());

        // Load the profile image URL using Glide (or any image loading library)
        String profileImageUrl = conversation.getProfileImageUrl(); // Get the profile image URL
        Glide.with(holder.profileImageView.getContext())
                .load(profileImageUrl) // Load the image URL
                .circleCrop() // Crop the image into a circle (optional)
                .into(holder.profileImageView); // Set the image to the ImageView

        // Set click listener for each conversation item
        holder.itemView.setOnClickListener(v -> {
            // Prepare the Intent to navigate to the ChatActivity
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("CHATROOM_ID", conversation.getId()); // Pass the chatroom ID
            intent.putExtra("SELLER_ID", getSellerId(conversation)); // Pass the seller's ID
            intent.putExtra("CURRENT_USER_ID", currentUserId); // Pass the current user ID (passed from activity)
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
        ImageView profileImageView; // Added an ImageView for the profile image

        public ConversationViewHolder(View itemView) {
            super(itemView);
            conversationNameTextView = itemView.findViewById(R.id.tv_conversation_name);
            lastMessageTextView = itemView.findViewById(R.id.tv_last_message);
            profileImageView = itemView.findViewById(R.id.profile_image); // Initialize the ImageView for profile image
        }
    }

    // Helper method to get the seller's ID (from the conversation's participants list)
    private String getSellerId(Conversation conversation) {
        List<String> participants = conversation.getParticipants();
        for (String participant : participants) {
            if (!participant.equals(currentUserId)) { // Ensure we don't return the current user's ID
                return participant; // The other participant is the seller
            }
        }
        return null;
    }

    // Method to update the conversation list (called after search or data update)
    public void updateConversations(List<Conversation> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }
}
