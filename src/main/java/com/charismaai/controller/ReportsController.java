package com.charismaai.controller;

import com.charismaai.model.PracticeSession;
import com.charismaai.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ReportsController {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private com.charismaai.repository.UserRepository userRepository;

    @Autowired
    private com.charismaai.service.PdfService pdfService;

    @GetMapping("/reports/export")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.InputStreamResource> exportPdf() {
        // Get Current User
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String username = auth.getName();
        com.charismaai.model.User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PracticeSession> sessions = sessionRepository.findByUser(currentUser,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        java.io.ByteArrayInputStream bis = pdfService.generateReport(sessions);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=my_report.pdf");

        return org.springframework.http.ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(new org.springframework.core.io.InputStreamResource(bis));
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        // Get Current User
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String username = auth.getName();
        com.charismaai.model.User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch sessions sorted by date (oldest first for chart)
        List<PracticeSession> sessions = sessionRepository.findByUser(currentUser,
                Sort.by(Sort.Direction.ASC, "createdDate"));

        // Calculate Average Scores
        double averageScore = 0.0;
        double averageAudioScore = 0.0;
        if (!sessions.isEmpty()) {
            double totalScore = sessions.stream().mapToDouble(PracticeSession::getEyeContactScore).sum();
            double totalAudioScore = sessions.stream().mapToDouble(PracticeSession::getAudioScore).sum();

            averageScore = Math.round((totalScore / sessions.size()) * 10.0) / 10.0;
            averageAudioScore = Math.round((totalAudioScore / sessions.size()) * 10.0) / 10.0;
        }

        // Determine Insight Message
        String insightMessage;
        if (sessions.isEmpty()) {
            insightMessage = "Start your first practice session today!";
        } else if (averageScore < 50) {
            insightMessage = "Keep practicing! Focus on maintaining steady eye contact.";
        } else if (averageScore < 80) {
            insightMessage = "Good progress! You are getting better.";
        } else {
            insightMessage = "Excellent work! Your charisma is growing fast.";
        }

        // Prepare Chart Data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        List<String> chartLabels = sessions.stream()
                .map(s -> s.getCreatedDate().format(formatter))
                .collect(Collectors.toList());
        List<Double> chartData = sessions.stream()
                .map(PracticeSession::getEyeContactScore)
                .collect(Collectors.toList());

        // Reverse sessions list for the table (newest first)
        List<PracticeSession> recentSessions = new ArrayList<>(sessions);
        java.util.Collections.reverse(recentSessions);

        model.addAttribute("sessions", recentSessions);
        model.addAttribute("averageScore", averageScore);
        model.addAttribute("averageAudioScore", averageAudioScore);
        model.addAttribute("insightMessage", insightMessage);
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        return "reports";
    }
}
