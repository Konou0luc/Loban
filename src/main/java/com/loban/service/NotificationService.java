package com.loban.service;

import com.loban.domain.Notification;
import com.loban.domain.User;
import com.loban.dto.NotificationResponse;
import com.loban.repository.NotificationRepository;
import com.loban.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notifyUser(Long userId, String title, String message, String type) {
        User user = userRepository.findById(userId).orElseThrow();
        Notification n = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .readFlag(false)
                .type(type)
                .build();
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId).orElseThrow();
        if (!n.getUser().getId().equals(userId)) {
            return;
        }
        n.setReadFlag(true);
    }

    @Transactional
    public void markAllRead(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        notificationRepository.findByUserOrderByCreatedAtDesc(user).forEach(x -> x.setReadFlag(true));
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return notificationRepository.countByUserAndReadFlagIsFalse(user);
    }
}
