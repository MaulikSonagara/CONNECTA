package com.example.connecta666620de.model;

import android.net.Uri;
import com.google.firebase.Timestamp;

public class UserModel {
    String firstName;
    String lastName;
    String userName;
    String email;
    Timestamp createdAt;
    String bio;  // New field
    String image; // New field (if needed)

    public UserModel() { }

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
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
