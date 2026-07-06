package com.timegrid.service;

import com.timegrid.entity.TimeEntry;
import com.timegrid.entity.User;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ForbiddenException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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

    @Transactional(readOnly = true)
    public List<TimeEntry> findOwn(LocalDate from, LocalDate to) {
        return timeEntryRepository.findByUserIdAndWorkDateBetweenAndDeletedAtIsNullOrderByWorkDateAscStartTimeAsc(
                currentUser.userId(), from, to);
    }

    @Transactional
    public TimeEntry create(LocalDate workDate, LocalTime startTime, LocalTime endTime, int breakMinutes,
            TimeEntryType type, String notes) {
        User user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new NotFoundException("User not found.", currentUser.userId()));

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

        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry submit(UUID id) {
        TimeEntry entry = findEditable(id);
        entry.setStatus(TimeEntryStatus.SUBMITTED);
        entry.setRejectionReason(null);
        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry approve(UUID id) {
        TimeEntry entry = findSubmitted(id);
        entry.setStatus(TimeEntryStatus.APPROVED);
        entry.setApprover(userRepository.getReferenceById(currentUser.userId()));
        entry.setApprovedAt(Instant.now());
        entry.setRejectionReason(null);
        return timeEntryRepository.save(entry);
    }

    @Transactional
    public TimeEntry reject(UUID id, String reason) {
        TimeEntry entry = findSubmitted(id);
        entry.setStatus(TimeEntryStatus.REJECTED);
        entry.setRejectionReason(reason);
        return timeEntryRepository.save(entry);
    }

    // TODO: os checks de aprovacao sao fixos aqui; passar para um motor de workflow quando existir implementacao
    private TimeEntry findSubmitted(UUID id) {
        TimeEntry entry = timeEntryRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("Time entry not found.", id));
        if (entry.getStatus() != TimeEntryStatus.SUBMITTED) {
            throw new BusinessRuleException("The entry is not in submit status.");
        }
        if (entry.getUser().getId().equals(currentUser.userId())) {
            throw new ForbiddenException("You cannot approve or reject your own entries.");
        }
        return entry;
    }

    private TimeEntry findEditable(UUID id) {
        TimeEntry entry = timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("Time entry not found.", id));
        if (entry.getStatus() != TimeEntryStatus.DRAFT && entry.getStatus() != TimeEntryStatus.REJECTED) {
            throw new BusinessRuleException("Only draft or rejected entries can be edited, deleted or submitted.");
        }
        return entry;
    }

    private void validateTimes(LocalTime startTime, LocalTime endTime, int breakMinutes) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessRuleException("The end time must be after the start time.");
        }
        long duration = Duration.between(startTime, endTime).toMinutes();
        if (breakMinutes >= duration) {
            throw new BusinessRuleException("The break must be shorter than the entry duration.");
        }
    }

    private void checkOverlap(UUID userId, LocalDate workDate, LocalTime startTime, LocalTime endTime, UUID excludeId) {
        boolean overlaps = timeEntryRepository.findByUserIdAndWorkDateAndDeletedAtIsNull(userId, workDate).stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .anyMatch(existing -> existing.getStartTime().isBefore(endTime)
                        && existing.getEndTime().isAfter(startTime));
        if (overlaps) {
            throw new BusinessRuleException("There is already an entry with an overlapping time range on this day.");
        }
    }

}
