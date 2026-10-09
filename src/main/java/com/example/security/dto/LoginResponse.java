package com.example.security.dto;

public record LoginResponse(
        String token,
        String type,
        String username,
        String role
) {}
