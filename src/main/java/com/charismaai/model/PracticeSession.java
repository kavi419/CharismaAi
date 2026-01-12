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
    private double smileScore; // New Field: Emotion
    private double postureScore; // New Field: Confidence
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
    public PracticeSession(String videoPath, double eyeContactScore, double audioScore, double smileScore,
            double postureScore, int pauseCount, String feedback,
            String transcript,
            LocalDateTime createdDate) {
        this.videoPath = videoPath;
        this.eyeContactScore = eyeContactScore;
        this.audioScore = audioScore;
        this.smileScore = smileScore;
        this.postureScore = postureScore;
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

    public double getSmileScore() {
        return smileScore;
    }

    public void setSmileScore(double smileScore) {
        this.smileScore = smileScore;
    }

    public double getPostureScore() {
        return postureScore;
    }

    public void setPostureScore(double postureScore) {
        this.postureScore = postureScore;
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

    // Badge Logic (Computed, not stored)
    public java.util.List<String> getBadges() {
        java.util.List<String> badges = new java.util.ArrayList<>();

        if (this.eyeContactScore >= 80)
            badges.add("👁️ Focus Master");
        if (this.audioScore >= 75)
            badges.add("🎤 Smooth Talker");
        if (this.smileScore >= 40)
            badges.add("😊 Charming Vibe");
        if (this.postureScore >= 80)
            badges.add("🦁 Confident Leader");
        if (this.eyeContactScore + this.audioScore + this.smileScore + this.postureScore >= 340) { // Approx 85 avg * 4
            // Or use locally calculated avg if not stored.
            // Let's use the explicit average calculation to be safe or assuming standard
            // weighting
            double avg = (this.eyeContactScore + this.audioScore + this.smileScore + this.postureScore) / 4.0;
            if (avg >= 85)
                badges.add("👑 Charisma Legend");
        }

        return badges;
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
