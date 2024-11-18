package com.example.gearup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
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
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });


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

        // Search EditText
        searchEditText = findViewById(R.id.et_search);

        // Add a listener to handle search input
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Do nothing here, or use it if necessary
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString();
                filterConversations(query); // Filter based on the search query
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing here, or use it if necessary
            }
        });

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
                            String otherParticipantId = null;
                            for (String participant : participants) {
                                if (participant != null && !participant.equals(currentUserId)) {
                                    otherParticipantId = participant;
                                    break;
                                }
                            }

                            if (otherParticipantId != null) {
                                checkParticipantTypeAndUpdateConversation(otherParticipantId, conversationId, participants, lastMessage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationListActivity.this, "Error loading conversations", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkParticipantTypeAndUpdateConversation(String otherParticipantId, String conversationId, List<String> participants, String lastMessage) {
        db.collection("sellers").document(otherParticipantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String shopName = documentSnapshot.getString("shopName");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        if (shopName != null) {
                            Conversation conversation = new Conversation(conversationId, participants, lastMessage, shopName, profileImageUrl);
                            conversations.add(conversation);
                            conversationAdapter.notifyDataSetChanged();
                        }
                    } else {
                        fetchBuyerDetailsAndUpdateConversation(otherParticipantId, conversationId, participants, lastMessage);
                    }
                })
                .addOnFailureListener(e -> {
                    fetchBuyerDetailsAndUpdateConversation(otherParticipantId, conversationId, participants, lastMessage);
                });
    }

    private void fetchBuyerDetailsAndUpdateConversation(String buyerId, String conversationId, List<String> participants, String lastMessage) {
        db.collection("buyers").document(buyerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        if (firstName != null && lastName != null) {
                            String buyerFullName = firstName + " " + lastName;
                            Conversation conversation = new Conversation(conversationId, participants, lastMessage, buyerFullName, profileImageUrl);
                            conversations.add(conversation);
                            conversationAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(ConversationListActivity.this, "Participant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationListActivity.this, "Error fetching buyer data", Toast.LENGTH_SHORT).show();
                });
    }

    // Filter conversations based on the search query
    private void filterConversations(String query) {
        List<Conversation> filteredConversations = new ArrayList<>();
        for (Conversation conversation : conversations) {
            if (conversation.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredConversations.add(conversation);
            }
        }
        conversationAdapter.updateConversations(filteredConversations);
    }
}
