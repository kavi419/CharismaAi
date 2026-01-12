package com.charismaai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class PracticeController {

    private static final String UPLOAD_DIR = "uploads";

    @org.springframework.beans.factory.annotation.Autowired
    private com.charismaai.repository.SessionRepository sessionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.charismaai.repository.UserRepository userRepository;

    @GetMapping("/practice")
    public String practice() {
        return "practice";
    }

    @PostMapping("/upload-video")
    @ResponseBody
    public String uploadVideo(@RequestParam("video") MultipartFile video) {
        if (video.isEmpty()) {
            return "{\"error\": \"Failed to upload video: File is empty\"}";
        }

        try {
            // Create upload directory if it doesn't exist
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String fileName = "video_" + System.currentTimeMillis() + ".webm";
            Path path = Paths.get(UPLOAD_DIR, fileName);
            File savedFile = path.toFile();

            // Save file
            Files.write(path, video.getBytes());

            // Run Python Analysis Script
            // using "py" instead of "python" to avoid Windows Store alias issues
            ProcessBuilder processBuilder = new ProcessBuilder("py", "analyze.py", savedFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true); // Merge stderr into stdout

            Process process = processBuilder.start();

            // Read output from Python script
            String result = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Parse the JSON result to get score and feedback
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(result);

                    double score = node.get("eye_contact_score").asDouble();
                    double audioScore = node.get("audio_score").asDouble();
                    int pauseCount = node.get("pause_count").asInt();
                    String feedback = node.get("feedback").asText();
                    String transcript = "";
                    if (node.has("transcript")) {
                        transcript = node.get("transcript").asText();
                    }

                    // Get Current User
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    String username = auth.getName();
                    com.charismaai.model.User currentUser = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // Save to Database
                    com.charismaai.model.PracticeSession session = new com.charismaai.model.PracticeSession(
                            fileName, score, audioScore, pauseCount, feedback, transcript,
                            java.time.LocalDateTime.now());
                    session.setUser(currentUser);
                    sessionRepository.save(session);

                } catch (Exception e) {
                    System.err.println("Error saving to DB: " + e.getMessage());
                    // Don't fail the request if DB save fails, just log it
                }

                return result; // Return the JSON from Python
            } else {
                return "{\"error\": \"Analysis failed\", \"details\": \"" + result.replace("\"", "'").replace("\n", " ")
                        + "\"}";
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "{\"error\": \"Server error: " + e.getMessage() + "\"}";
        }
    }
}
