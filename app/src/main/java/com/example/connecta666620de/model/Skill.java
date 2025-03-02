package com.example.connecta666620de.model;

public class Skill {
    private String title;
    private String description;
    private String skillLevel; // Updated to use dropdown
    private String experienceLevel; // New field

    // Default constructor (required for Firebase)
    public Skill() {}

    // Parameterized constructor
    public Skill(String title, String description, String skillLevel, String experienceLevel) {
        this.title = title;
        this.description = description;
        this.skillLevel = skillLevel;
        this.experienceLevel = experienceLevel;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
}