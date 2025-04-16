package com.example.connecta666620de.model;

import java.io.Serializable;
import java.util.List;

public class Post implements Serializable {
    private String postId;
    private String userId;
    private long timestamp;
    private String type;
    private String imageUrl;
    private String caption;
    private String question;
    private List<String> options;
    private String correctAnswer;

    public Post() {}

    // For General Image Post
    public Post(String postId, String userId, long timestamp, String type, String imageUrl, String caption) {
        this.postId = postId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.type = type;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    // For Doubt Post
    public Post(String postId, String userId, long timestamp, String type, String question) {
        this.postId = postId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.type = type;
        this.question = question;
    }

    // For Quiz Post
    public Post(String postId, String userId, long timestamp, String type, String question, List<String> options, String correctAnswer) {
        this.postId = postId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.type = type;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}