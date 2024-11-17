package com.example.gearup;

import java.util.List;

public class Conversation {

    private String id;
    private List<String> participants;
    private String lastMessage;

    public Conversation(String id, List<String> participants, String lastMessage) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = lastMessage;
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
}
