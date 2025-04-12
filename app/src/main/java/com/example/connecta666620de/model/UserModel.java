package com.example.connecta666620de.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String firstName;
    private String uId;
    private String lastName;
    private String userName;
    private String email;
    private Timestamp createdAt;
    private String bio;
    private String image;

    private String lastMessage;
    private long timestamp;

    private long posts, follower, following;  // Changed from String to long

    // Default constructor (required for Firebase)
    public UserModel() {}

    public UserModel(String firstName, String uId, String lastName, String userName, String email, Timestamp createdAt, String bio, String image, long posts, long follower, long following) {
        this.firstName = firstName;
        this.uId = uId;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.createdAt = createdAt;
        this.bio = bio;
        this.image = image;
        this.posts = posts;
        this.follower = follower;
        this.following = following;
    }

    // Getters and Setters
    public String getUid() {
        return uId;
    }

    public void setUid(String uid) {
        uId = uid;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public long getFollower() {
        return follower;
    }

    public void setFollower(long follower) {
        this.follower = follower;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    // Add these getters and setters
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
