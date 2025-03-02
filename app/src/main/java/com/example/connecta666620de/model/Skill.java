package com.example.connecta666620de.model;

public class Skill {
    private String title;
    private String description;
    private String level;

    // Default constructor (required for Firebase)
    public Skill() {}

    // Parameterized constructor
    public Skill(String title, String description, String level) {
        this.title = title;
        this.description = description;
        this.level = level;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
