package com.loban.dto;

import com.loban.domain.Notification;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        boolean read,
        String type,
        String createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.isReadFlag(),
                n.getType(),
                n.getCreatedAt().toString()
        );
    }
}
