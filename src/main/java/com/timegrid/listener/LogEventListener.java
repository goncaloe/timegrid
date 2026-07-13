package com.timegrid.listener;

import com.timegrid.enums.LogAction;
import com.timegrid.event.AbsenceStatusChanged;
import com.timegrid.event.TimeEntryStatusChanged;
import com.timegrid.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final LogService logService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTimeEntryStatusChanged(TimeEntryStatusChanged event) {
        logService.record(event.organizationId(), event.changedBy(), LogAction.TIME_ENTRY_STATUS_CHANGED,
                "TIME_ENTRY", event.timeEntryId(), describe(event.from(), event.to(), event.comment()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAbsenceStatusChanged(AbsenceStatusChanged event) {
        logService.record(event.organizationId(), event.changedBy(), LogAction.ABSENCE_STATUS_CHANGED,
                "ABSENCE", event.absenceId(), describe(event.from(), event.to(), event.comment()));
    }

    private static String describe(Enum<?> from, Enum<?> to, String comment) {
        String transition = from + " -> " + to;
        return comment != null ? transition + " (" + comment + ")" : transition;
    }

}