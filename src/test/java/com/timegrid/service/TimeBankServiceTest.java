package com.timegrid.service;

import com.timegrid.dto.TimeBankBalance;
import com.timegrid.dto.TimeBankDay;
import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.TimeEntry;
import com.timegrid.entity.User;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import com.timegrid.enums.TimeBankReason;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import com.timegrid.service.HolidayService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeBankServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private AbsenceRequestRepository absenceRequestRepository;

    @Mock
    private HolidayService holidayService;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private TimeBankService timeBankService;

    private User userWithId(UUID id, int dailyWorkMinutes) {
        Organization organization = new Organization();
        ReflectionTestUtils.setField(organization, "id", UUID.randomUUID());

        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        user.setOrganization(organization);
        user.setDailyWorkMinutes(dailyWorkMinutes);
        return user;
    }

    private TimeEntry approvedEntry(LocalDate date, LocalTime start, LocalTime end, int breakMinutes) {
        TimeEntry entry = new TimeEntry();
        entry.setWorkDate(date);
        entry.setStartTime(start);
        entry.setEndTime(end);
        entry.setBreakMinutes(breakMinutes);
        entry.setType(TimeEntryType.OFFICE);
        entry.setStatus(TimeEntryStatus.APPROVED);
        return entry;
    }

    @Test
    void computesExpectedWorkedAndBalanceAccountingForWeekendsHolidaysAndAbsences() {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId, 480);
        LocalDate from = LocalDate.of(2026, 9, 5); // sabado
        LocalDate to = LocalDate.of(2026, 9, 11); // sexta
        LocalDate holiday = LocalDate.of(2026, 9, 8); // terca
        LocalDate absenceDay = LocalDate.of(2026, 9, 9); // quarta

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(currentUser.userId()).thenReturn(userId);
        when(holidayService.datesBetween(user.getOrganization().getId(), from, to)).thenReturn(Set.of(holiday));

        AbsenceRequest absence = new AbsenceRequest();
        absence.setStartDate(absenceDay);
        absence.setEndDate(absenceDay);
        absence.setType(AbsenceType.SICK);
        absence.setStatus(AbsenceStatus.APPROVED);
        when(absenceRequestRepository.findApprovedOverlapping(userId, from, to)).thenReturn(List.of(absence));

        when(timeEntryRepository.findByUserIdAndWorkDateBetweenAndStatusAndDeletedAtIsNull(
                userId, from, to, TimeEntryStatus.APPROVED)).thenReturn(List.of(
                        approvedEntry(LocalDate.of(2026, 9, 7), LocalTime.of(9, 0), LocalTime.of(18, 0), 60),
                        approvedEntry(LocalDate.of(2026, 9, 10), LocalTime.of(9, 0), LocalTime.of(19, 0), 60)));

        TimeBankBalance balance = timeBankService.balanceForSelf(from, to);

        assertThat(balance.expectedMinutes()).isEqualTo(3 * 480); // seg, qui, sex
        assertThat(balance.workedMinutes()).isEqualTo(480 + 540);
        assertThat(balance.balanceMinutes()).isEqualTo(480 + 540 - 3 * 480);
        assertThat(balance.days()).hasSize(7);

        TimeBankDay saturday = dayOf(balance, LocalDate.of(2026, 9, 5));
        assertThat(saturday.reason()).isEqualTo(TimeBankReason.WEEKEND);
        assertThat(saturday.expectedMinutes()).isZero();

        TimeBankDay tuesday = dayOf(balance, holiday);
        assertThat(tuesday.reason()).isEqualTo(TimeBankReason.HOLIDAY);

        TimeBankDay wednesday = dayOf(balance, absenceDay);
        assertThat(wednesday.reason()).isEqualTo(TimeBankReason.ABSENCE);

        TimeBankDay friday = dayOf(balance, LocalDate.of(2026, 9, 11));
        assertThat(friday.reason()).isNull();
        assertThat(friday.expectedMinutes()).isEqualTo(480);
        assertThat(friday.workedMinutes()).isZero();
    }

    @Test
    void rejectsPeriodEndingBeforeItStarts() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithId(userId, 480)));

        assertThatThrownBy(() -> timeBankService.balanceForSelf(
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 1)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void balanceForUserRejectsUserFromAnotherOrganization() {
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        when(currentUser.organizationId()).thenReturn(organizationId);
        when(userRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(userId, organizationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeBankService.balanceForUser(
                userId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 11)))
                .isInstanceOf(NotFoundException.class);
    }

    private static TimeBankDay dayOf(TimeBankBalance balance, LocalDate date) {
        return balance.days().stream()
                .filter(day -> day.date().equals(date))
                .findFirst()
                .orElseThrow();
    }

}