package com.fintrack.controller;

import com.fintrack.dto.NotificationResponse;
import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        User user = getAuthenticatedUser(userPrincipal);
        List<NotificationResponse> list = notificationService.getNotifications(user, unreadOnly);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched notifications", list));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        notificationService.markAsRead(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Notification marked as read", "Read"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getAuthenticatedUser(userPrincipal);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "All notifications marked as read", "All Read"));
    }
}
