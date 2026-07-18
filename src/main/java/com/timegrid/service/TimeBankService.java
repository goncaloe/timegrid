package com.timegrid.service;

import com.timegrid.dto.TimeBankBalance;
import com.timegrid.dto.TimeBankDay;
import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.TimeEntry;
import com.timegrid.entity.User;
import com.timegrid.enums.TimeBankReason;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeBankService {

    private final UserRepository userRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final HolidayService holidayService;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public TimeBankBalance balanceForSelf(LocalDate from, LocalDate to) {
        return balance(findUser(currentUser.userId()), from, to);
    }

    @Transactional(readOnly = true)
    public TimeBankBalance balanceForUser(UUID userId, LocalDate from, LocalDate to) {
        User user = userRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(userId, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("user.not-found", userId));
        return balance(user, from, to);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.not-found", userId));
    }

    private TimeBankBalance balance(User user, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessRuleException("time-bank.invalid-period");
        }

        Set<LocalDate> holidays = holidayService.datesBetween(user.getOrganization().getId(), from, to);
        List<AbsenceRequest> approvedAbsences = absenceRequestRepository.findApprovedOverlapping(user.getId(), from,
                to);
        Map<LocalDate, Integer> workedByDay = workedMinutesByDay(user.getId(), from, to);

        List<TimeBankDay> days = new ArrayList<>();
        int expectedTotal = 0;
        int workedTotal = 0;
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            TimeBankReason reason = reasonFor(date, holidays, approvedAbsences);
            int expected = reason == null ? user.getDailyWorkMinutes() : 0;
            int worked = workedByDay.getOrDefault(date, 0);
            expectedTotal += expected;
            workedTotal += worked;
            days.add(new TimeBankDay(date, expected, worked, reason));
        }

        return new TimeBankBalance(user.getId(), from, to, expectedTotal, workedTotal, workedTotal - expectedTotal,
                days);
    }

    private TimeBankReason reasonFor(LocalDate date, Set<LocalDate> holidays, List<AbsenceRequest> approvedAbsences) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return TimeBankReason.WEEKEND;
        }
        if (holidays.contains(date)) {
            return TimeBankReason.HOLIDAY;
        }
        boolean onApprovedAbsence = approvedAbsences.stream()
                .anyMatch(absence -> !date.isBefore(absence.getStartDate()) && !date.isAfter(absence.getEndDate()));
        if (onApprovedAbsence) {
            return TimeBankReason.ABSENCE;
        }
        return null;
    }

    private Map<LocalDate, Integer> workedMinutesByDay(UUID userId, LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> result = new HashMap<>();
        for (TimeEntry entry : timeEntryRepository.findByUserIdAndWorkDateBetweenAndStatusAndDeletedAtIsNull(
                userId, from, to, TimeEntryStatus.APPROVED)) {
            int minutes = (int) Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes()
                    - entry.getBreakMinutes();
            result.merge(entry.getWorkDate(), minutes, Integer::sum);
        }
        return result;
    }

}