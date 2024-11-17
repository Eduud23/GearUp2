package com.example.gearup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Message> messages;
    private String currentUserId; // To distinguish sender from receiver

    // Constructor
    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getContent());

        if (message.getSenderId().equals(currentUserId)) {
            // If message is from current user (sender)
            holder.messageTextView.setBackgroundResource(R.drawable.message_sender_background);
            holder.messageTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
        } else {
            // If message is from other user (receiver)
            holder.messageTextView.setBackgroundResource(R.drawable.message_receiver_background);
            holder.messageTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.tv_message);
        }
    }
}
