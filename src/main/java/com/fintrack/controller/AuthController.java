package com.fintrack.controller;

import com.fintrack.dto.*;
import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.JwtTokenProvider;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserProfileResponse>> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        UserProfileResponse profile = userService.signup(signupRequest);
        ApiResponse<UserProfileResponse> response = new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", profile);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse jwtResponse = userService.login(loginRequest);
        ApiResponse<JwtAuthenticationResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Login successful", jwtResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshJwtToken(@Valid @RequestBody RefreshTokenRequest request) {
        if (tokenProvider.validateToken(request.getRefreshToken())) {
            Long userId = tokenProvider.getUserIdFromJWT(request.getRefreshToken());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for refresh token"));

            if (!user.isActive()) {
                return new ResponseEntity<>(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "Account is inactive", null), HttpStatus.FORBIDDEN);
            }

            String newAccessToken = tokenProvider.generateTokenFromUserId(userId);
            
            java.util.List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(java.util.stream.Collectors.toList());

            JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse(newAccessToken, request.getRefreshToken(), user.getId(), user.getUsername(), user.getEmail(), roles);
            ApiResponse<JwtAuthenticationResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Token refreshed successfully", jwtResponse);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<JwtAuthenticationResponse> response = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid refresh token", null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(), "Password reset successfully. Check log for details.", "Check server log for TempPass123");
        return ResponseEntity.ok(response);
    }
}
