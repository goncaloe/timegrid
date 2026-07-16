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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.MailSendException;
import org.springframework.transaction.PlatformTransactionManager;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUser currentUser;
    @Mock
    private NotificationChannel webSocket;
    @Mock
    private NotificationChannel email;
    @Mock
    private PlatformTransactionManager transactionManager;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository, currentUser,
                List.of(email, webSocket), transactionManager);
    }

    @Test
    void notifySavesAndDeliversThroughEveryChannel() {
        User recipient = new User();
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(recipient));

        Notification saved = notificationService.notify(USER_ID, ORG_ID, NotificationCategory.ABSENCE,
                NotificationType.STATUS_CHANGED, "Pedido de ausência aprovado", "Foi aprovado.", "/absences/1");

        assertThat(saved.getRecipientId()).isEqualTo(USER_ID);
        assertThat(saved.getOrganizationId()).isEqualTo(ORG_ID);
        assertThat(saved.getCategory()).isEqualTo(NotificationCategory.ABSENCE);
        assertThat(saved.getLink()).isEqualTo("/absences/1");
        assertThat(saved.isRead()).isFalse();
        verify(email).deliver(saved, NotificationType.STATUS_CHANGED, recipient);
        verify(webSocket).deliver(saved, NotificationType.STATUS_CHANGED, recipient);
    }

    @Test
    void failingChannelDoesNotStopTheOthers() {
        User recipient = new User();
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(recipient));
        when(email.name()).thenReturn("email");
        doThrow(new MailSendException("smtp down")).when(email).deliver(any(), any(), any());

        Notification saved = notificationService.notify(USER_ID, ORG_ID, NotificationCategory.TIME_ENTRY,
                NotificationType.PENDING_APPROVAL, "Time entries awaiting approval", "...", null);

        verify(webSocket).deliver(saved, NotificationType.PENDING_APPROVAL, recipient);
    }

    @Test
    void notifyStillSavesWhenRecipientIsGone() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        notificationService.notify(USER_ID, ORG_ID, NotificationCategory.SYSTEM, NotificationType.STATUS_CHANGED,
                "t", "m", null);

        verify(notificationRepository).save(any(Notification.class));
        verify(webSocket, never()).deliver(any(), any(), any());
        verify(email, never()).deliver(any(), any(), any());
    }

    @Test
    void findOwnFiltersUnreadWhenAsked() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(List.of(new Notification()));
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.findByRecipientIdAndReadAtIsNull(USER_ID, pageable)).thenReturn(page);

        assertThat(notificationService.findOwn(true, pageable)).isSameAs(page);
        verify(notificationRepository, never()).findByRecipientId(any(), any());
    }

    @Test
    void findOwnReturnsEverythingByDefault() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Notification> page = new PageImpl<>(List.of());
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.findByRecipientId(USER_ID, pageable)).thenReturn(page);

        assertThat(notificationService.findOwn(false, pageable)).isSameAs(page);
    }

    @Test
    void countUnreadIsForCurrentUser() {
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.countByRecipientIdAndReadAtIsNull(USER_ID)).thenReturn(3L);

        assertThat(notificationService.countUnread()).isEqualTo(3L);
    }

    @Test
    void markReadSetsReadAt() {
        UUID id = UUID.randomUUID();
        Notification notification = new Notification();
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.findByIdAndRecipientId(id, USER_ID)).thenReturn(Optional.of(notification));

        Notification result = notificationService.markRead(id);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    void markReadKeepsFirstReadAt() {
        UUID id = UUID.randomUUID();
        Instant firstRead = Instant.parse("2026-09-01T10:00:00Z");
        Notification notification = new Notification();
        notification.setReadAt(firstRead);
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.findByIdAndRecipientId(id, USER_ID)).thenReturn(Optional.of(notification));

        assertThat(notificationService.markRead(id).getReadAt()).isEqualTo(firstRead);
    }

    @Test
    void markReadOfSomeoneElsesNotificationIsNotFound() {
        UUID id = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.findByIdAndRecipientId(id, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markRead(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void markAllReadOnlyTouchesCurrentUser() {
        when(currentUser.userId()).thenReturn(USER_ID);
        when(notificationRepository.markAllRead(eq(USER_ID), any(Instant.class))).thenReturn(2);

        assertThat(notificationService.markAllRead()).isEqualTo(2);
    }

}