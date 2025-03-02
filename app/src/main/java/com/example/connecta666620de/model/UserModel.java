package com.example.connecta666620de.model;

import com.google.firebase.Timestamp;

public class UserModel {
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private Timestamp createdAt;
    private String bio;
    private String image;

    // Default constructor (required for Firebase)
    public UserModel() {}

    // Parameterized constructor
    public UserModel(String firstName, String lastName, String userName, String email, Timestamp createdAt, String bio, String image) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.createdAt = createdAt;
        this.bio = bio;
        this.image = image;
    }

    // Getters and Setters
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
}