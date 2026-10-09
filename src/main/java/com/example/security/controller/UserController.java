package com.example.security.controller;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users/profile")
    public ResponseEntity<?> profile(Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "createdAt", user.getCreatedAt()
        ));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> users() {

        List<Map<String, Object>> result = userRepository.findAll()
                .stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole().name()))
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDashboard() {
        return ResponseEntity.ok(
                Map.of("message", "Welcome to ADMIN dashboard"));
    }

    @GetMapping("/user/dashboard")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<?> userDashboard() {
        return ResponseEntity.ok(
                Map.of("message", "Welcome to USER dashboard"));
    }
}
