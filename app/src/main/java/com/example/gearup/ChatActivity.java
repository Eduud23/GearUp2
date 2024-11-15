package com.example.gearup;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private FirebaseFirestore db;
    private String sellerId, currentUserId, chatId;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize UI components
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageEditText = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.btn_send);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get sellerId and currentUserId from intent
        sellerId = getIntent().getStringExtra("SELLER_ID");
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        if (sellerId == null || currentUserId == null) {
            Toast.makeText(this, "Error: Missing user or seller info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Create a unique chatId
        chatId = createChatId(sellerId, currentUserId);

        // Set up RecyclerView with LinearLayoutManager
        messageAdapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Keep messages scrolled to the bottom
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Load messages from Firestore
        loadMessages();

        // Set up the send button click listener
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private String createChatId(String sellerId, String currentUserId) {
        // Create a unique chatId using the seller and user IDs (order doesn't matter)
        if (sellerId.compareTo(currentUserId) > 0) {
            return sellerId + "_" + currentUserId;
        } else {
            return currentUserId + "_" + sellerId;
        }
    }

    private void loadMessages() {
        // Retrieve the messages from Firestore
        db.collection("messages").document(chatId)
                .collection("chat")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messageList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        messageList.add(message);
                    }
                    messageAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new message object
        Message message = new Message(currentUserId, messageText, System.currentTimeMillis());

        // Save the message to Firestore in the chat collection
        db.collection("messages").document(chatId)
                .collection("chat")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageEditText.setText(""); // Clear input field
                    updateChatroom(); // Create or update the chatroom metadata
                    loadMessages(); // Reload messages
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void updateChatroom() {
        // Create a chatroom metadata object
        Chatroom chatroom = new Chatroom(chatId, currentUserId, System.currentTimeMillis(), currentUserId, sellerId);

        // Create or update the chatroom in the Firestore "chatrooms" collection
        db.collection("chatrooms")
                .document(chatId)
                .set(chatroom) // This will create a new chatroom or update an existing one
                .addOnSuccessListener(aVoid -> {
                    // Optionally, log or do something after the chatroom is created/updated
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error updating chatroom", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
