package com.timegrid.service;

import com.timegrid.entity.Notification;
import com.timegrid.entity.User;
import com.timegrid.enums.NotificationCategory;
import com.timegrid.enums.NotificationType;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.NotificationRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final List<NotificationChannel> channels;
    private final TransactionTemplate newTransaction;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository,
            CurrentUser currentUser, List<NotificationChannel> channels,
            PlatformTransactionManager transactionManager) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.channels = channels;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // chamado a partir de listeners AFTER_COMMIT: grava numa transação nova e só entrega depois do commit,
	// para quem recebe pelo websocket já encontrar a notificação no GET
    public Notification notify(UUID recipientId, UUID organizationId, NotificationCategory category,
            NotificationType type, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setOrganizationId(organizationId);
        notification.setCategory(category);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        Notification saved = newTransaction.execute(status -> notificationRepository.save(notification));

        userRepository.findById(recipientId).ifPresent(recipient -> deliver(saved, type, recipient));
        return saved;
    }

    private void deliver(Notification notification, NotificationType type, User recipient) {
        for (NotificationChannel channel : channels) {
            try {
                channel.deliver(notification, type, recipient);
            } catch (RuntimeException e) {
                log.warn("Failed to deliver notification {} via the channel {}: {}", notification.getId(), channel.name(),
                        e.getMessage());
            }
        }
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