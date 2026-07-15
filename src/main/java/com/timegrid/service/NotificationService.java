package com.timegrid.service;

import com.timegrid.entity.Notification;
import com.timegrid.enums.NotificationCategory;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.NotificationRepository;
import com.timegrid.security.CurrentUser;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUser currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification notify(UUID recipientId, UUID organizationId, NotificationCategory category, String title,
            String message, String link) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setOrganizationId(organizationId);
        notification.setCategory(category);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<Notification> findOwn(boolean unreadOnly, Pageable pageable) {
        UUID userId = currentUser.userId();
        return unreadOnly
                ? notificationRepository.findByRecipientIdAndReadAtIsNull(userId, pageable)
                : notificationRepository.findByRecipientId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return notificationRepository.countByRecipientIdAndReadAtIsNull(currentUser.userId());
    }

    @Transactional
    public Notification markRead(UUID id) {
        Notification notification = notificationRepository.findByIdAndRecipientId(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("notification.not-found", id));
        if (!notification.isRead()) {
            notification.setReadAt(Instant.now());
        }
        return notification;
    }

    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(currentUser.userId(), Instant.now());
    }

}