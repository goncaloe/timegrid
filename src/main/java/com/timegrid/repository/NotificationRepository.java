package com.timegrid.repository;

import com.timegrid.entity.Notification;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadAtIsNull(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

    Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.readAt = :readAt
            WHERE n.recipientId = :recipientId
            AND n.readAt IS NULL
            """)
    int markAllRead(UUID recipientId, Instant readAt);

}