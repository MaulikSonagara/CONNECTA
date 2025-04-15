package com.example.connecta666620de.model;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private long timestamp; // Add this field to store the message timestamp

    private boolean isseen;


    public Chat() {
        // Default constructor
    }

    public Chat(String sender, String receiver, String message, long timestamp, boolean isseen) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
        this.isseen = isseen;

    }

    // Getters and Setters

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}