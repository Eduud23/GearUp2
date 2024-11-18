package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private EditText searchEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_seller);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get current user ID (Seller)
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

        // Initialize ProgressBar for loading indicator
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(ProgressBar.VISIBLE);  // Show progress bar while loading conversations

        // Search EditText
        searchEditText = findViewById(R.id.et_search);

        // Add a listener to handle search input
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Do nothing here
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String query = charSequence.toString();
                filterConversations(query); // Filter based on the search query
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing here
            }
        });

        // Load conversations for the current user
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
                                // Fetch the buyer's name and profile image and update the conversation
                                fetchBuyerDetailsAndUpdateConversation(buyerId, conversationId, participants, lastMessage);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(ConversationSellerActivity.this, "Error loading conversations", Toast.LENGTH_SHORT).show();
                });
    }

    // Fetch the buyer's name (firstName and lastName), profile image, and update the conversation
    private void fetchBuyerDetailsAndUpdateConversation(String buyerId, String conversationId, List<String> participants, String lastMessage) {
        db.collection("buyers").document(buyerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the buyer's details
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl"); // Profile image URL

                        // Log the fetched values for debugging
                        Log.d("ConversationSeller", "Profile Image URL: " + profileImageUrl);
                        Log.d("ConversationSeller", "Buyer Name: " + firstName + " " + lastName);

                        // Combine first and last names to form the conversation name
                        String buyerName = firstName + " " + lastName;

                        // Check if the profile image URL is null or empty
                        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                            Log.d("ConversationSeller", "Profile Image URL is missing.");
                            profileImageUrl = ""; // Set to empty if no URL found
                        }

                        // Add the new Conversation object with the buyer's details
                        Conversation conversation = new Conversation(conversationId, participants, lastMessage, buyerName, profileImageUrl);
                        conversations.add(conversation);

                        // Notify adapter of data change
                        conversationSellerAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(ProgressBar.GONE); // Hide progress bar after data is loaded
                    } else {
                        // Handle the case where the buyer's data is not found
                        Toast.makeText(ConversationSellerActivity.this, "Buyer not found", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(ProgressBar.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(ConversationSellerActivity.this, "Error fetching buyer data", Toast.LENGTH_SHORT).show();
                    Log.e("ConversationSeller", "Error fetching buyer data", e); // Log the error
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
        conversationSellerAdapter.updateConversations(filteredConversations);
    }
}
