package com.example.gearup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

        // Get current user ID from Intent or shared preferences
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        // Initialize RecyclerView
        conversationRecyclerView = findViewById(R.id.rv_conversations);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(conversations);
        conversationRecyclerView.setAdapter(conversationAdapter);

        // Load conversations for the current user
        loadConversations();
    }

    // Fetch conversations that include the current user as a participant
    private void loadConversations() {
        db.collection("chatrooms") // <-- Using 'chatrooms' collection
                .whereArrayContains("participants", currentUserId) // <-- Checking for current user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    conversations.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String conversationId = document.getId();
                        List<String> participants = (List<String>) document.get("participants");
                        String lastMessage = document.getString("lastMessage");

                        // Create a Conversation object and add it to the list
                        if (participants != null) {
                            conversations.add(new Conversation(conversationId, participants, lastMessage));
                        }
                    }
                    conversationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConversationListActivity.this, "Error loading conversations", Toast.LENGTH_SHORT).show();
                });
    }
}
