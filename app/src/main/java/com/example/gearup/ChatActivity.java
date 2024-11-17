package com.example.gearup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messages = new ArrayList<>();
    private FirebaseFirestore db;
    private String chatroomId;
    private String currentUserId;
    private String sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get passed data from the Intent
        chatroomId = getIntent().getStringExtra("CHATROOM_ID");
        sellerId = getIntent().getStringExtra("SELLER_ID");
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        // Initialize UI components
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageEditText = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.btn_send);

        // Set up RecyclerView for chat messages
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages, currentUserId);
        messagesRecyclerView.setAdapter(chatAdapter);

        // Set the click listener for the send button
        sendButton.setOnClickListener(v -> sendMessage());

        // If chatroomId is passed, load messages directly
        if (chatroomId != null && !chatroomId.isEmpty()) {
            loadMessages();
        } else {
            // Check if a chatroom already exists between these two participants
            checkAndCreateChatroom();
        }
    }

    // Send message method
    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            Message message = new Message(currentUserId, messageText, System.currentTimeMillis());

            if (chatroomId == null || chatroomId.isEmpty()) {
                // If no chatroomId, create a new chatroom and send the message
                createChatroomAndSendMessage(message);
            } else {
                // Send message directly to Firestore
                sendMessageToFirestore(message);
            }
        } else {
            Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    // Check if a chatroom already exists with the same participants
    private void checkAndCreateChatroom() {
        // Fetch all chatrooms to check for existing chatrooms with the same participants
        db.collection("chatrooms")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean chatroomExists = false;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        List<String> participants = (List<String>) document.get("participants");

                        // Check if sellerId is in the participants list
                        if (participants != null && participants.contains(sellerId)) {
                            // If the chatroom already exists, use the existing one
                            chatroomId = document.getId();
                            chatroomExists = true;
                            break;
                        }
                    }

                    if (!chatroomExists) {
                        // If no existing chatroom is found, create a new chatroom
                        createChatroomAndSendMessage(new Message(currentUserId, "", System.currentTimeMillis()));
                    } else {
                        // Load messages for the existing chatroom
                        loadMessages();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error checking for existing chatroom", Toast.LENGTH_SHORT).show();
                });
    }

    // Create a new chatroom and send the first message
    private void createChatroomAndSendMessage(Message message) {
        Map<String, Object> chatroomData = new HashMap<>();
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(sellerId);

        chatroomData.put("participants", participants);
        chatroomData.put("lastMessage", message.getContent());
        chatroomData.put("timestamp", System.currentTimeMillis());

        db.collection("chatrooms")
                .add(chatroomData)
                .addOnSuccessListener(documentReference -> {
                    chatroomId = documentReference.getId();
                    sendMessageToFirestore(message);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error creating chatroom", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    // Send message to Firestore
    private void sendMessageToFirestore(Message message) {
        db.collection("chatrooms").document(chatroomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    updateChatroomLastMessage(message.getContent());
                    messageEditText.setText("");
                    loadMessages();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

    // Load messages from Firestore
    private void loadMessages() {
        db.collection("chatrooms").document(chatroomId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messages.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        messages.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                });
    }

    // Update last message in chatroom document
    private void updateChatroomLastMessage(String lastMessage) {
        Map<String, Object> chatroomData = new HashMap<>();
        chatroomData.put("lastMessage", lastMessage);
        chatroomData.put("timestamp", System.currentTimeMillis());

        db.collection("chatrooms").document(chatroomId)
                .update(chatroomData)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated last message
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error updating last message", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
