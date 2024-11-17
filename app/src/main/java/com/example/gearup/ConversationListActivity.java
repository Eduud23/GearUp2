package com.example.gearup;

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

public class ConversationListActivity extends AppCompatActivity {

    private RecyclerView conversationRecyclerView;
    private ConversationAdapter conversationAdapter;
    private List<Conversation> conversations = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get current user ID from Intent
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        if (currentUserId == null) {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no user is authenticated
            return;
        }

        // Initialize RecyclerView
        conversationRecyclerView = findViewById(R.id.rv_conversations);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the ConversationAdapter
        conversationAdapter = new ConversationAdapter(conversations, currentUserId);
        conversationRecyclerView.setAdapter(conversationAdapter);

        // Load conversations for the current user
        loadConversations();
    }

    // Fetch conversations that include the current user as a participant
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
                            // Find the other participant (buyer or seller) and fetch details
                            String otherParticipantId = null;
                            for (String participant : participants) {
                                // Add null check before comparing participant
                                if (participant != null && !participant.equals(currentUserId)) {
                                    otherParticipantId = participant;
                                    break;
                                }
                            }

                            if (otherParticipantId != null) {
                                // Check if the other participant is a seller or a buyer
                                checkParticipantTypeAndUpdateConversation(otherParticipantId, conversationId, participants, lastMessage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationListActivity.this, "Error loading conversations", Toast.LENGTH_SHORT).show();
                });
    }

    // Check if the participant is a seller or a buyer
    private void checkParticipantTypeAndUpdateConversation(String otherParticipantId, String conversationId, List<String> participants, String lastMessage) {
        // First, check if the participant is a seller
        db.collection("sellers").document(otherParticipantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String shopName = documentSnapshot.getString("shopName");
                        if (shopName != null) {
                            // Create the conversation for a seller
                            Conversation conversation = new Conversation(conversationId, participants, lastMessage, shopName);
                            conversations.add(conversation);
                            conversationAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // If not a seller, check if the participant is a buyer
                        fetchBuyerDetailsAndUpdateConversation(otherParticipantId, conversationId, participants, lastMessage);
                    }
                })
                .addOnFailureListener(e -> {
                    // If there's an error fetching seller data, try the buyer data
                    fetchBuyerDetailsAndUpdateConversation(otherParticipantId, conversationId, participants, lastMessage);
                });
    }

    // Fetch the buyer's first name and last name and update the conversation
    private void fetchBuyerDetailsAndUpdateConversation(String buyerId, String conversationId, List<String> participants, String lastMessage) {
        db.collection("buyers").document(buyerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        if (firstName != null && lastName != null) {
                            // Combine the first and last name of the buyer
                            String buyerFullName = firstName + " " + lastName;

                            // Create the conversation for a buyer
                            Conversation conversation = new Conversation(conversationId, participants, lastMessage, buyerFullName);
                            conversations.add(conversation);
                            conversationAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle the case where neither seller nor buyer data is found
                        Toast.makeText(ConversationListActivity.this, "Participant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationListActivity.this, "Error fetching buyer data", Toast.LENGTH_SHORT).show();
                });
    }
}
