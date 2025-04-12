package com.example.connecta666620de.model;

public class Chatlist {
    private String id; // This will store the other user's UID
    private long timestamp; // Last message timestamp
    private String lastMessage; // Last message preview

    public Chatlist() {
        // Default constructor required for Firebase
    }

    public Chatlist(String id, long timestamp, String lastMessage) {
        this.id = id;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}