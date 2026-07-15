package com.timegrid.listener;

import com.timegrid.common.Messages;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.NotificationCategory;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.event.AbsenceStatusChanged;
import com.timegrid.event.TimeEntryStatusChanged;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.service.NotificationService;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
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
    private final Messages messages;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTimeEntryStatusChanged(TimeEntryStatusChanged event) {
        if (event.ownerId().equals(event.changedBy()) || !TIME_ENTRY_OWNER_STATUSES.contains(event.to())) {
            return;
        }
        String date = timeEntryRepository.findById(event.timeEntryId())
                .map(entry -> entry.getWorkDate().format(DATE))
                .orElse("");
        String key = "notification.time-entry." + event.to().name();
        notificationService.notify(event.ownerId(), event.organizationId(), NotificationCategory.TIME_ENTRY,
                messages.get(key + ".title", LOCALE),
                messages.get(key + ".message", LOCALE, date, event.comment()),
                "/time-entries/" + event.timeEntryId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAbsenceStatusChanged(AbsenceStatusChanged event) {
        if (event.ownerId().equals(event.changedBy()) || !ABSENCE_OWNER_STATUSES.contains(event.to())) {
            return;
        }
        String period = absenceRequestRepository.findById(event.absenceId())
                .map(request -> request.getStartDate().format(DATE) + " - " + request.getEndDate().format(DATE))
                .orElse("");
        String key = "notification.absence." + event.to().name();
        notificationService.notify(event.ownerId(), event.organizationId(), NotificationCategory.ABSENCE,
                messages.get(key + ".title", LOCALE),
                messages.get(key + ".message", LOCALE, period, event.comment()),
                "/absences/" + event.absenceId());
    }

}