package com.timegrid.service;

import com.timegrid.entity.TimeEntry;
import com.timegrid.entity.User;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import com.timegrid.enums.WorkflowEntityType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final ApprovalPolicy approvalPolicy;
    private final WorkflowService workflowService;
    private final StatusHistoryService statusHistoryService;

    @Transactional(readOnly = true)
    public List<TimeEntry> findOwn(LocalDate from, LocalDate to) {
        return timeEntryRepository.findByUserIdAndWorkDateBetweenAndDeletedAtIsNullOrderByWorkDateAscStartTimeAsc(
                currentUser.userId(), from, to);
    }

    @Transactional(readOnly = true)
    public List<TimeEntry> findPending(UUID teamId) {
        return timeEntryRepository.findPending(currentUser.organizationId(), TimeEntryStatus.SUBMITTED, teamId);
    }

    @Transactional
    public TimeEntry create(LocalDate workDate, LocalTime startTime, LocalTime endTime, int breakMinutes,
            TimeEntryType type, String notes) {
        User user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new NotFoundException("user.not-found", currentUser.userId()));

        validateTimes(startTime, endTime, breakMinutes);
        checkOverlap(user.getId(), workDate, startTime, endTime, null);

        TimeEntry entry = new TimeEntry();
        entry.setUser(user);
        entry.setOrganization(user.getOrganization());
        entry.setWorkDate(workDate);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        entry.setBreakMinutes(breakMinutes);
        entry.setType(type);
        entry.setNotes(notes);
        entry.setStatus(TimeEntryStatus.DRAFT);

        TimeEntry saved = timeEntryRepository.save(entry);
        statusHistoryService.record(WorkflowEntityType.TIME_ENTRY, saved.getId(), null, TimeEntryStatus.DRAFT.name(),
                null);
        return saved;
    }

    @Transactional
    public TimeEntry update(UUID id, LocalDate workDate, LocalTime startTime, LocalTime endTime, int breakMinutes,
            TimeEntryType type, String notes) {
        TimeEntry entry = findEditable(id);

        validateTimes(startTime, endTime, breakMinutes);
        checkOverlap(entry.getUser().getId(), workDate, startTime, endTime, id);

        entry.setWorkDate(workDate);
        entry.setStartTime(startTime);
        entry.setEndTime(endTime);
        entry.setBreakMinutes(breakMinutes);
        entry.setType(type);
        entry.setNotes(notes);
        if (entry.getStatus() == TimeEntryStatus.REJECTED) {
            entry.setStatus(TimeEntryStatus.DRAFT);
            entry.setRejectionReason(null);
        }

        return timeEntryRepository.save(entry);
    }

    @Transactional
    public void delete(UUID id) {
        TimeEntry entry = findEditable(id);
        entry.setDeletedAt(Instant.now());
    }

    @Transactional
    public TimeEntry restore(UUID id) {
        TimeEntry entry = timeEntryRepository.findByIdAndUserId(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("time-entry.not-found", id));
        entry.setDeletedAt(null);
        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry submit(UUID id) {
        TimeEntry entry = timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("time-entry.not-found", id));
        TimeEntryStatus from = entry.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.TIME_ENTRY, from.name(), TimeEntryStatus.SUBMITTED.name(),
                currentUser.roles());
        entry.setStatus(TimeEntryStatus.SUBMITTED);
        entry.setRejectionReason(null);
        TimeEntry saved = timeEntryRepository.save(entry);
        statusHistoryService.record(WorkflowEntityType.TIME_ENTRY, saved.getId(), from.name(),
                TimeEntryStatus.SUBMITTED.name(), null);
        return saved;
    }

    @Transactional
    public TimeEntry approve(UUID id) {
        TimeEntry entry = findForApproval(id);
        TimeEntryStatus from = entry.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.TIME_ENTRY, from.name(), TimeEntryStatus.APPROVED.name(),
                currentUser.roles());
        entry.setStatus(TimeEntryStatus.APPROVED);
        entry.setApprover(userRepository.getReferenceById(currentUser.userId()));
        entry.setApprovedAt(Instant.now());
        entry.setRejectionReason(null);
        TimeEntry saved = timeEntryRepository.save(entry);
        statusHistoryService.record(WorkflowEntityType.TIME_ENTRY, saved.getId(), from.name(),
                TimeEntryStatus.APPROVED.name(), null);
        return saved;
    }

    @Transactional
    public TimeEntry reject(UUID id, String reason) {
        TimeEntry entry = findForApproval(id);
        TimeEntryStatus from = entry.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.TIME_ENTRY, from.name(), TimeEntryStatus.REJECTED.name(),
                currentUser.roles());
        entry.setStatus(TimeEntryStatus.REJECTED);
        entry.setRejectionReason(reason);
        TimeEntry saved = timeEntryRepository.save(entry);
        statusHistoryService.record(WorkflowEntityType.TIME_ENTRY, saved.getId(), from.name(),
                TimeEntryStatus.REJECTED.name(), reason);
        return saved;
    }

    private TimeEntry findForApproval(UUID id) {
        TimeEntry entry = timeEntryRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("time-entry.not-found", id));
        approvalPolicy.checkNotOwnRequest(entry.getUser().getId(), "time-entry.approve-self");
        return entry;
    }

    private TimeEntry findEditable(UUID id) {
        TimeEntry entry = timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("time-entry.not-found", id));
        if (entry.getStatus() != TimeEntryStatus.DRAFT && entry.getStatus() != TimeEntryStatus.REJECTED) {
            throw new BusinessRuleException("time-entry.not-editable");
        }
        return entry;
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime, int breakMinutes) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessRuleException("time-entry.invalid-times");
        }
        long duration = Duration.between(startTime, endTime).toMinutes();
        if (breakMinutes >= duration) {
            throw new BusinessRuleException("time-entry.invalid-break");
        }
    }

    private void checkOverlap(UUID userId, LocalDate workDate, LocalTime startTime, LocalTime endTime, UUID excludeId) {
        boolean overlaps = timeEntryRepository.findByUserIdAndWorkDateAndDeletedAtIsNull(userId, workDate).stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .anyMatch(existing -> existing.getStartTime().isBefore(endTime)
                        && existing.getEndTime().isAfter(startTime));
        if (overlaps) {
            throw new BusinessRuleException("time-entry.overlap");
        }
    }

}