package com.example.gearup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private String buyerId;

    private TextView senderNameTextView; // Reference for the sender's name TextView
    private ImageView profileImageView; // Reference for the profile image ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get passed data from the Intent
        chatroomId = getIntent().getStringExtra("CHATROOM_ID");
        sellerId = getIntent().getStringExtra("SELLER_ID");
        buyerId = getIntent().getStringExtra("BUYER_ID");
        currentUserId = getIntent().getStringExtra("CURRENT_USER_ID");

        // Initialize UI components
        messagesRecyclerView = findViewById(R.id.rv_messages);
        messageEditText = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.btn_send);
        senderNameTextView = findViewById(R.id.tv_sender_name); // Find the sender's name TextView
        profileImageView = findViewById(R.id.profile_image); // Find the profile image ImageView

        // Set up RecyclerView for chat messages
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages, currentUserId);
        messagesRecyclerView.setAdapter(chatAdapter);

        // Set the click listener for the send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Dynamically load the name of the other participant (either buyer or seller)
        loadOtherParticipantName();

        // If chatroomId is passed, load messages directly, otherwise check for existing chatroom
        if (chatroomId != null && !chatroomId.isEmpty()) {
            loadMessages();
        } else {
            // Chatroom is not created yet, so no need to auto-create it until a message is sent
        }
    }

    private void loadMessages() {
        db.collection("chatrooms").document(chatroomId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messages.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Message message = document.toObject(Message.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    markMessagesAsRead(); // Mark messages as read when the user opens the chat
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            String receiverId = currentUserId.equals(sellerId) ? buyerId : sellerId;

            Message message = new Message(currentUserId, receiverId, messageText, System.currentTimeMillis(), "unread");

            // If no chatroom exists yet, create one
            if (chatroomId == null || chatroomId.isEmpty()) {
                createChatroomAndSendMessage(message);
            } else {
                // If chatroom already exists, send the message to Firestore
                sendMessageToFirestore(message);
            }
        } else {
            Toast.makeText(ChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void createChatroomAndSendMessage(Message message) {
        Map<String, Object> chatroomData = new HashMap<>();
        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(sellerId);

        chatroomData.put("participants", participants);
        chatroomData.put("lastMessage", message.getContent());
        chatroomData.put("timestamp", System.currentTimeMillis());

        // Create a new chatroom and then send the message
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

    private void sendMessageToFirestore(Message message) {
        db.collection("chatrooms").document(chatroomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    updateChatroomLastMessage(message.getContent());
                    messageEditText.setText("");
                    loadMessages(); // Reload messages to show the new one
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

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

    private void markMessagesAsRead() {
        for (Message message : messages) {
            if (message.getStatus().equals("unread") && message.getReceiverId().equals(currentUserId)) {
                message.setStatus("read");

                db.collection("chatrooms").document(chatroomId)
                        .collection("messages")
                        .whereEqualTo("timestamp", message.getTimestamp())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                                db.collection("chatrooms").document(chatroomId)
                                        .collection("messages").document(documentSnapshot.getId())
                                        .update("status", "read")
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully updated message status
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ChatActivity.this, "Error updating message status", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, "Error updating message status", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void loadOtherParticipantName() {
        if (currentUserId == null || sellerId == null) {
            Toast.makeText(ChatActivity.this, "User or Seller ID is null", Toast.LENGTH_SHORT).show();
            return; // Exit the method early if IDs are missing
        }

        String otherParticipantId = currentUserId.equals(sellerId) ? buyerId : sellerId;

        if (otherParticipantId == null) {
            Toast.makeText(ChatActivity.this, "Other participant ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (otherParticipantId.equals(sellerId)) {
            db.collection("sellers").document(sellerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String shopName = documentSnapshot.getString("shopName");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            senderNameTextView.setText(shopName != null ? shopName : "Shop");

                            if (profileImageUrl != null) {
                                Glide.with(ChatActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.gear)
                                        .into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.gear);
                            }
                        } else {
                            senderNameTextView.setText("Shop");
                            profileImageView.setImageResource(R.drawable.gear);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Error fetching seller's name and image", Toast.LENGTH_SHORT).show();
                    });
        } else if (otherParticipantId.equals(buyerId)) {
            db.collection("buyers").document(otherParticipantId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            senderNameTextView.setText(firstName + " " + lastName);

                            if (profileImageUrl != null) {
                                Glide.with(ChatActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.gear)
                                        .into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.gear);
                            }
                        } else {
                            senderNameTextView.setText("Buyer");
                            profileImageView.setImageResource(R.drawable.gear);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Error fetching buyer's name and image", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
