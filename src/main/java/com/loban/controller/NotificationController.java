package com.loban.controller;

import com.loban.dto.NotificationResponse;
import com.loban.security.AppUserDetails;
import com.loban.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal AppUserDetails principal) {
        return notificationService.listForUser(principal.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(@AuthenticationPrincipal AppUserDetails principal) {
        return Map.of("count", notificationService.unreadCount(principal.getId()));
    }

    @PatchMapping("/{id}/read")
    public void markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        notificationService.markRead(id, principal.getId());
    }

    @PostMapping("/read-all")
    public void markAll(@AuthenticationPrincipal AppUserDetails principal) {
        notificationService.markAllRead(principal.getId());
    }
}
