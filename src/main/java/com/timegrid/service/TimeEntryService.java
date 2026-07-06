package com.timegrid.service;

import com.timegrid.entity.TimeEntry;
import com.timegrid.entity.User;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
                .orElseThrow(() -> new NotFoundException("user.not-found", currentUser.userId()));

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

}
