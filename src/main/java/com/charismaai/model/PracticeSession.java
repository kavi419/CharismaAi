package com.charismaai.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class PracticeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoPath;
    private double eyeContactScore;
    private double audioScore; // New Field: Fluency
    private int pauseCount; // New Field: Awkward Pauses
    private String feedback;

    @jakarta.persistence.Column(length = 5000)
    private String transcript;

    private LocalDateTime createdDate;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "user_id")
    private User user;

    public PracticeSession() {
        // No-args constructor for JPA
    }

    // Updated Constructor
    public PracticeSession(String videoPath, double eyeContactScore, double audioScore, int pauseCount, String feedback,
            String transcript,
            LocalDateTime createdDate) {
        this.videoPath = videoPath;
        this.eyeContactScore = eyeContactScore;
        this.audioScore = audioScore;
        this.pauseCount = pauseCount;
        this.feedback = feedback;
        this.transcript = transcript;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public double getEyeContactScore() {
        return eyeContactScore;
    }

    public void setEyeContactScore(double eyeContactScore) {
        this.eyeContactScore = eyeContactScore;
    }

    public double getAudioScore() {
        return audioScore;
    }

    public void setAudioScore(double audioScore) {
        this.audioScore = audioScore;
    }

    public int getPauseCount() {
        return pauseCount;
    }

    public void setPauseCount(int pauseCount) {
        this.pauseCount = pauseCount;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
