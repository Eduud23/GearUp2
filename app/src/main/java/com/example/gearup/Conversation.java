package com.example.gearup;

import java.util.List;

public class Conversation {

    private String id;
    private List<String> participants;
    private String lastMessage;
    private String shopName; // New field for shopName

    public Conversation(String id, List<String> participants, String lastMessage, String shopName) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.shopName = shopName; // Initialize shopName
    }

    public String getId() {
        return id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getShopName() {
        return shopName; // Getter for shopName
    }
}
