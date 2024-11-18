package com.example.gearup;

import java.util.List;

public class Conversation {

    private String id;
    private List<String> participants;
    private String lastMessage;
    private String shopName; // Field to store shopName for sellers
    private String profileImageUrl; // Field for profile image URL of the participant

    // Constructor for creating a Conversation object (with shopName for seller and profileImageUrl)
    public Conversation(String id, List<String> participants, String lastMessage, String shopName, String profileImageUrl) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = lastMessage;
        this.shopName = shopName; // Initialize shopName for sellers
        this.profileImageUrl = profileImageUrl; // Initialize profileImageUrl for displaying image
    }

    // Getter for conversation ID
    public String getId() {
        return id;
    }

    // Getter for participants list
    public List<String> getParticipants() {
        return participants;
    }

    // Getter for the last message in the conversation
    public String getLastMessage() {
        return lastMessage;
    }

    // Getter for the shop name (only for sellers)
    public String getShopName() {
        return shopName;
    }

    // Getter for profile image URL (for displaying profile image)
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Method to get the name for search purposes
    // This method returns the shop name for sellers, or a default value if not a seller
    public String getName() {
        return shopName != null && !shopName.isEmpty() ? shopName : "Unknown Name"; // Return shopName if it's a seller, else "Unknown Name"
    }

    // Setter for profile image URL (in case you need to modify it later)
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
