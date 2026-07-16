package com.timegrid.listener;

import com.timegrid.common.Messages;
import com.timegrid.entity.User;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.NotificationCategory;
import com.timegrid.enums.NotificationType;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.event.AbsenceStatusChanged;
import com.timegrid.event.TimeEntryStatusChanged;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.service.NotificationService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private static final Locale LOCALE = Locale.ENGLISH;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Set<TimeEntryStatus> TIME_ENTRY_OWNER_STATUSES = Set.of(TimeEntryStatus.APPROVED,
            TimeEntryStatus.REJECTED);
    private static final Set<AbsenceStatus> ABSENCE_OWNER_STATUSES = Set.of(AbsenceStatus.APPROVED,
            AbsenceStatus.REJECTED, AbsenceStatus.CANCELLED);

    private final NotificationService notificationService;
    private final TimeEntryRepository timeEntryRepository;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final UserRepository userRepository;
    private final Messages messages;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTimeEntryStatusChanged(TimeEntryStatusChanged event) {
        String date = timeEntryRepository.findById(event.timeEntryId())
                .map(entry -> entry.getWorkDate().format(DATE))
                .orElse("");
        String link = "/time-entries/" + event.timeEntryId();

        if (event.to() == TimeEntryStatus.SUBMITTED) {
            String owner = ownerName(event.ownerId());
            for (User approver : approvers(event.organizationId(), event.ownerId())) {
                notificationService.notify(approver.getId(), event.organizationId(), NotificationCategory.TIME_ENTRY,
                        NotificationType.PENDING_APPROVAL,
                        messages.get("notification.time-entry.SUBMITTED.title", LOCALE),
                        messages.get("notification.time-entry.SUBMITTED.message", LOCALE, owner, date),
                        link);
            }
            return;
        }
        if (event.ownerId().equals(event.changedBy()) || !TIME_ENTRY_OWNER_STATUSES.contains(event.to())) {
            return;
        }
        String key = "notification.time-entry." + event.to().name();
        notificationService.notify(event.ownerId(), event.organizationId(), NotificationCategory.TIME_ENTRY,
                NotificationType.STATUS_CHANGED,
                messages.get(key + ".title", LOCALE),
                messages.get(key + ".message", LOCALE, date, event.comment()),
                link);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAbsenceStatusChanged(AbsenceStatusChanged event) {
        String period = absenceRequestRepository.findById(event.absenceId())
                .map(request -> request.getStartDate().format(DATE) + " - " + request.getEndDate().format(DATE))
                .orElse("");
        String link = "/absences/" + event.absenceId();

        if (event.to() == AbsenceStatus.SUBMITTED) {
            String owner = ownerName(event.ownerId());
            for (User approver : approvers(event.organizationId(), event.ownerId())) {
                notificationService.notify(approver.getId(), event.organizationId(), NotificationCategory.ABSENCE,
                        NotificationType.PENDING_APPROVAL,
                        messages.get("notification.absence.SUBMITTED.title", LOCALE),
                        messages.get("notification.absence.SUBMITTED.message", LOCALE, owner, period),
                        link);
            }
            return;
        }
        if (event.ownerId().equals(event.changedBy()) || !ABSENCE_OWNER_STATUSES.contains(event.to())) {
            return;
        }
        String key = "notification.absence." + event.to().name();
        notificationService.notify(event.ownerId(), event.organizationId(), NotificationCategory.ABSENCE,
                NotificationType.STATUS_CHANGED,
                messages.get(key + ".title", LOCALE),
                messages.get(key + ".message", LOCALE, period, event.comment()),
                link);
    }

    private List<User> approvers(UUID organizationId, UUID ownerId) {
        List<User> managers = userRepository.findActiveByOrganizationIdAndRole(organizationId, "MANAGER").stream()
                .filter(user -> !user.getId().equals(ownerId))
                .toList();
        Optional<UUID> teamId = userRepository.findTeamIdById(ownerId);
        if (teamId.isPresent()) {
            List<User> teamManagers = managers.stream()
                    .filter(user -> user.getTeam() != null && user.getTeam().getId().equals(teamId.get()))
                    .toList();
            if (!teamManagers.isEmpty()) {
                return teamManagers;
            }
        }
        if (!managers.isEmpty()) {
            return managers;
        }
        return userRepository.findActiveByOrganizationIdAndRole(organizationId, "ADMIN").stream()
                .filter(user -> !user.getId().equals(ownerId))
                .toList();
    }

    private String ownerName(UUID ownerId) {
        return userRepository.findById(ownerId).map(User::getFullName).orElse("");
    }

}