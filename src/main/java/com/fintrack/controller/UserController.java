package com.fintrack.controller;

import com.fintrack.dto.ChangePasswordRequest;
import com.fintrack.dto.UpdateProfileRequest;
import com.fintrack.dto.UserProfileResponse;
import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getAuthenticatedUser(userPrincipal);
        UserProfileResponse profile = userService.getProfile(user);
        ApiResponse<UserProfileResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Fetched user profile successfully", profile);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        UserProfileResponse updated = userService.updateProfile(user, request);
        ApiResponse<UserProfileResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Updated user profile successfully", updated);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        userService.changePassword(user, request);
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(), "Password changed successfully", "Password updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getAuthenticatedUser(userPrincipal);
        userService.deactivateAccount(user);
        ApiResponse<String> response = new ApiResponse<>(HttpStatus.OK.value(), "Account deactivated successfully", "Account disabled");
        return ResponseEntity.ok(response);
    }
}
