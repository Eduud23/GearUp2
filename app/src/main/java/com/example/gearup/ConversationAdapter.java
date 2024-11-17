package com.example.gearup;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;

    public ConversationAdapter(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.conversationNameTextView.setText(getConversationName(conversation));
        holder.lastMessageTextView.setText(conversation.getLastMessage());

        // Set click listener for each conversation item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("CONVERSATION_ID", conversation.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    private String getConversationName(Conversation conversation) {
        List<String> participants = conversation.getParticipants();
        participants.removeIf(participant -> participant.equals("CURRENT_USER_ID")); // Remove current user ID
        return participants.isEmpty() ? "No other participants" : participants.get(0); // Assuming 1 other participant
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        TextView conversationNameTextView;
        TextView lastMessageTextView;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            conversationNameTextView = itemView.findViewById(R.id.tv_conversation_name);
            lastMessageTextView = itemView.findViewById(R.id.tv_last_message);
        }
    }
}
