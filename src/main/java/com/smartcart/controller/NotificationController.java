package com.smartcart.controller;

import com.smartcart.config.CurrentUser;
import com.smartcart.dto.ApiResponse;
import com.smartcart.model.Notification;
import com.smartcart.model.User;
import com.smartcart.repository.NotificationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final CurrentUser currentUser;

    public NotificationController(NotificationRepository notificationRepository, CurrentUser currentUser) {
        this.notificationRepository = notificationRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> all(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched", notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.get(request).getId())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> read(HttpServletRequest request, @PathVariable Long id) {
        User user = currentUser.get(request);
        return notificationRepository.findByIdAndUserId(id, user.getId())
                .map(n -> { n.setReadStatus(true); return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", notificationRepository.save(n))); })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.fail("Notification not found")));
    }
}
