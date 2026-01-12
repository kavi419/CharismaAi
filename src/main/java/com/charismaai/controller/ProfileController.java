package com.charismaai.controller;

import com.charismaai.model.User;
import com.charismaai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProfileController {

    @Autowired
    private com.charismaai.repository.UserRepository userRepository;

    @Autowired
    private com.charismaai.repository.SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        model.addAttribute("user", user);

        // Fetch user sessions
        java.util.List<com.charismaai.model.PracticeSession> sessions = sessionRepository
                .findByUserOrderByCreatedDateDesc(user);

        // Calculate Stats
        int totalSessions = sessions.size();
        double highestScore = 0;

        for (com.charismaai.model.PracticeSession session : sessions) {
            // Calculate overall score if not stored (or assume frontend logic match)
            // Using logic: (eye + audio + smile + posture) / 4
            double overall = (session.getEyeContactScore() + session.getAudioScore() + session.getSmileScore()
                    + session.getPostureScore()) / 4.0;
            if (overall > highestScore) {
                highestScore = overall;
            }
        }

        model.addAttribute("totalSessions", totalSessions);
        model.addAttribute("highestScore", (int) highestScore);

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();

        // Update fields
        user.setUsername(username);
        user.setEmail(email);

        // Update password only if provided
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);

        model.addAttribute("user", user);
        model.addAttribute("success", "Profile updated successfully!");
        return "profile";
    }
}
