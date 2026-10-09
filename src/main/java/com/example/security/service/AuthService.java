package com.example.security.service;

import com.example.security.dto.*;
import com.example.security.entity.Role;
import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import com.example.security.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = request.role() == null ? Role.USER : request.role();

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.username());

        String token = jwtService.generateToken(userDetails);

        User user = ((com.example.security.security.CustomUserDetails) userDetails)
                .getUser();

        return new LoginResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole().name());
    }
}
