package com.loban.service;

import com.loban.domain.Role;
import com.loban.domain.User;
import com.loban.dto.AuthResponse;
import com.loban.dto.LoginRequest;
import com.loban.dto.RegisterRequest;
import com.loban.exception.ApiException;
import com.loban.config.JwtTokenProvider;
import com.loban.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée");
        }
        boolean transporterProfileComplete = request.role() != Role.TRANSPORTER;
        User user = User.builder()
                .fullname(request.fullname())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .transporterProfileComplete(transporterProfileComplete)
                .build();
        userRepository.save(user);
        String token = jwtTokenProvider.createToken(user);
        User refreshed = userRepository.findById(user.getId()).orElseThrow();
        return AuthResponse.of(token, userService.buildUserResponse(refreshed));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        String token = jwtTokenProvider.createToken(user);
        return AuthResponse.of(token, userService.buildUserResponse(user));
    }
}
