package com.example.connecta666620de.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Post implements Serializable {
    private String postId;
    private String userId;
    private String type;
    private String caption;
    private String imageUrl;
    private String question;
    private List<String> options;
    private String correctAnswer;
    private long timestamp;
    private long likeCount;
    private List<String> likedBy;
    private long commentCount;

    public Post() {}

    public Post(String postId, String userId, String type, String caption, String imageUrl, String question, List<String> options, String correctAnswer, long timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.type = type;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.timestamp = timestamp;
        this.likeCount = 0;
        this.likedBy = new ArrayList<>();
        this.commentCount = 0;
    }

    // Getters and setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
}