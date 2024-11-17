package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConversationSellerActivity extends AppCompatActivity {

    private RecyclerView conversationRecyclerView;
    private ConversationSellerAdapter conversationSellerAdapter;
    private List<Conversation> conversations = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_seller);

        db = FirebaseFirestore.getInstance();

        // Get current user ID from Intent (Seller)
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        if (currentUserId == null) {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no user is authenticated
            return;
        }

        // Initialize RecyclerView
        conversationRecyclerView = findViewById(R.id.rv_conversations);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the ConversationSellerAdapter
        conversationSellerAdapter = new ConversationSellerAdapter(conversations, currentUserId);
        conversationRecyclerView.setAdapter(conversationSellerAdapter);

        // Load conversations for the current user (seller)
        loadConversations();
    }

    // Fetch conversations where the seller is a participant
    private void loadConversations() {
        db.collection("chatrooms") // Using 'chatrooms' collection
                .whereArrayContains("participants", currentUserId) // Checking for current user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    conversations.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String conversationId = document.getId();
                        List<String> participants = (List<String>) document.get("participants");
                        String lastMessage = document.getString("lastMessage");

                        if (participants != null) {
                            // Find the buyerId from the participants list
                            String buyerId = null;
                            for (String participant : participants) {
                                if (!participant.equals(currentUserId)) {
                                    buyerId = participant;
                                    break;
                                }
                            }

                            if (buyerId != null) {
                                // Fetch the buyer's name and update the conversation
                                fetchBuyerNameAndUpdateConversation(buyerId, conversationId, participants, lastMessage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationSellerActivity.this, "Error loading conversations", Toast.LENGTH_SHORT).show();
                });
    }

    // Fetch the buyer's name (firstName and lastName) from Firestore and update the conversation
    private void fetchBuyerNameAndUpdateConversation(String buyerId, String conversationId, List<String> participants, String lastMessage) {
        db.collection("buyers").document(buyerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        // Combine first and last names to form the conversation name
                        String buyerName = firstName + " " + lastName;

                        // Add a new Conversation object to the list with the buyer's name
                        Conversation conversation = new Conversation(conversationId, participants, lastMessage, buyerName);
                        conversations.add(conversation);

                        // Notify adapter of data change
                        conversationSellerAdapter.notifyDataSetChanged();
                    } else {
                        // Handle the case where the buyer's data is not found
                        Toast.makeText(ConversationSellerActivity.this, "Buyer not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationSellerActivity.this, "Error fetching buyer data", Toast.LENGTH_SHORT).show();
                });
    }

    // Assuming this method exists for sending a reply message from the seller's side
    private void sendMessage(String messageText, String conversationId, String receiverId) {
        if (!messageText.isEmpty() && receiverId != null) {
            // Create a new Message object with senderId (currentUserId), receiverId, message content, and timestamp
            Message message = new Message(currentUserId, receiverId, messageText, System.currentTimeMillis());

            // Send the message to Firestore in the appropriate chatroom and messages collection
            db.collection("chatrooms").document(conversationId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        // Update the last message in the chatroom document
                        updateLastMessageInChatroom(conversationId, messageText);
                        Toast.makeText(ConversationSellerActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ConversationSellerActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Update the last message in the chatroom document after a reply
    private void updateLastMessageInChatroom(String conversationId, String lastMessage) {
        db.collection("chatrooms").document(conversationId)
                .update("lastMessage", lastMessage, "timestamp", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated the last message
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationSellerActivity.this, "Error updating last message", Toast.LENGTH_SHORT).show();
                });
    }
}
