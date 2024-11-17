package com.example.gearup;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {

    private String chatRoomName;  // Chat room name
    private List<String> participants;  // Participants (user IDs)
    private List<Message> messages;  // Messages in the chat room

    // Default constructor for Firestore
    public ChatRoom() {
        // Firestore requires an empty constructor
    }

    // Constructor with participants and an optional chatRoomName
    public ChatRoom(List<String> participants) {
        this.chatRoomName = "Chat Room " + participants.get(0) + " & " + participants.get(1); // Default name using participants
        this.participants = participants;
        this.messages = new ArrayList<>();
    }

    // Getter and Setter methods

    public String getChatRoomName() {
        return chatRoomName;
    }

    public void setChatRoomName(String chatRoomName) {
        this.chatRoomName = chatRoomName;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
