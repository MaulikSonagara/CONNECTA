package com.example.connecta666620de.model;

import com.google.firebase.Timestamp;

public class Notification {
    private String notificationId;
    private String senderId;
    private String receiverId;
    private String type; // "follow", "like", "comment", etc.
    private String content;
    private String postId; // For post-related notifications
    private boolean isRead;
    private long timestamp;

    private boolean isIncoming; // true for notifications from others, false for self-generated

    public Notification() {
        // Required empty constructor for Firebase
    }

    // Update constructor
    public Notification(String senderId, String receiverId, String type, String content, String postId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.content = content;
        this.postId = postId;
        this.isRead = false;
        this.timestamp = System.currentTimeMillis(); // Set current time
    }

    // Getters and setters
    // Add getter and setter
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getPostId() { return postId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isIncoming() { return isIncoming; }
    public void setIncoming(boolean incoming) { isIncoming = incoming; }
}