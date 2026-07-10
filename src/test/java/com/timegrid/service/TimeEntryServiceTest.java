package com.timegrid.service;

import com.timegrid.entity.Organization;
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
import com.timegrid.service.ApprovalPolicy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private ApprovalPolicy approvalPolicy;

    @InjectMocks
    private TimeEntryService timeEntryService;

    private User userWithId(UUID id) {
        User user = new User();
        user.setOrganization(new Organization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void createRejectsWhenEndIsNotAfterStart() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithId(userId)));

        assertThatThrownBy(() -> timeEntryService.create(
                LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(9, 0), 0, TimeEntryType.OFFICE, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createRejectsWhenBreakIsNotShorterThanDuration() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithId(userId)));

        assertThatThrownBy(() -> timeEntryService.create(
                LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), 60, TimeEntryType.OFFICE, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createRejectsOverlappingEntry() {
        UUID userId = UUID.randomUUID();
        LocalDate workDate = LocalDate.now();
        TimeEntry existing = new TimeEntry();
        existing.setStartTime(LocalTime.of(9, 0));
        existing.setEndTime(LocalTime.of(13, 0));

        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithId(userId)));
        when(timeEntryRepository.findByUserIdAndWorkDateAndDeletedAtIsNull(userId, workDate))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> timeEntryService.create(
                workDate, LocalTime.of(12, 0), LocalTime.of(14, 0), 0, TimeEntryType.OFFICE, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createSucceedsAndStartsAsDraft() {
        UUID userId = UUID.randomUUID();
        LocalDate workDate = LocalDate.now();
        User user = userWithId(userId);

        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUserIdAndWorkDateAndDeletedAtIsNull(userId, workDate))
                .thenReturn(List.of());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntry created = timeEntryService.create(
                workDate, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, TimeEntryType.REMOTE, "remote work");

        assertThat(created.getStatus()).isEqualTo(TimeEntryStatus.DRAFT);
        assertThat(created.getOrganization()).isEqualTo(user.getOrganization());
    }

    @Test
    void updateRejectsWhenEntryIsNotDraftOrRejected() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setStatus(TimeEntryStatus.SUBMITTED);

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> timeEntryService.update(
                id, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(17, 0), 0, TimeEntryType.OFFICE, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateResetsRejectedEntryToDraft() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        LocalDate workDate = LocalDate.now();
        TimeEntry entry = new TimeEntry();
        entry.setUser(userWithId(userId));
        entry.setStatus(TimeEntryStatus.REJECTED);
        entry.setRejectionReason("motivo antigo");

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(entry));
        when(timeEntryRepository.findByUserIdAndWorkDateAndDeletedAtIsNull(userId, workDate)).thenReturn(List.of());
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntry updated = timeEntryService.update(
                id, workDate, LocalTime.of(9, 0), LocalTime.of(17, 0), 0, TimeEntryType.OFFICE, null);

        assertThat(updated.getStatus()).isEqualTo(TimeEntryStatus.DRAFT);
        assertThat(updated.getRejectionReason()).isNull();
    }

    @Test
    void deleteRejectsWhenEntryIsNotDraftOrRejected() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setStatus(TimeEntryStatus.APPROVED);

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> timeEntryService.delete(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void restoreClearsDeletedAt() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setDeletedAt(java.time.Instant.now());

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntry restored = timeEntryService.restore(id);

        assertThat(restored.getDeletedAt()).isNull();
    }

    @Test
    void submitMovesDraftToSubmitted() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setStatus(TimeEntryStatus.DRAFT);

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntry submitted = timeEntryService.submit(id);

        assertThat(submitted.getStatus()).isEqualTo(TimeEntryStatus.SUBMITTED);
    }

    @Test
    void submitRejectsWhenAlreadySubmitted() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setStatus(TimeEntryStatus.SUBMITTED);

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> timeEntryService.submit(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void approveSetsApproverAndStatus() {
        UUID organizationId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setUser(userWithId(ownerId));
        entry.setStatus(TimeEntryStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(currentUser.userId()).thenReturn(approverId);
        when(timeEntryRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(entry));
        when(userRepository.getReferenceById(approverId)).thenReturn(userWithId(approverId));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntry approved = timeEntryService.approve(id);

        assertThat(approved.getStatus()).isEqualTo(TimeEntryStatus.APPROVED);
        assertThat(approved.getApprover().getId()).isEqualTo(approverId);
        assertThat(approved.getApprovedAt()).isNotNull();
    }

    @Test
    void approveRejectsOwnEntry() {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setUser(userWithId(userId));
        entry.setStatus(TimeEntryStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(timeEntryRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(entry));
        doThrow(new ForbiddenException("time-entry.approve-self"))
                .when(approvalPolicy).checkNotOwnRequest(userId, "time-entry.approve-self");

        assertThatThrownBy(() -> timeEntryService.approve(id))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveRejectsWhenNotSubmitted() {
        UUID organizationId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setUser(userWithId(UUID.randomUUID()));
        entry.setStatus(TimeEntryStatus.DRAFT);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(timeEntryRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> timeEntryService.approve(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectSetsReasonAndStatus() {
        UUID organizationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setUser(userWithId(ownerId));
        entry.setStatus(TimeEntryStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(timeEntryRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(entry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TimeEntry rejected = timeEntryService.reject(id, "There are missing justified hours");

        assertThat(rejected.getStatus()).isEqualTo(TimeEntryStatus.REJECTED);
        assertThat(rejected.getRejectionReason()).isEqualTo("There are missing justified hours");
    }

    @Test
    void rejectRejectsOwnEntry() {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setUser(userWithId(userId));
        entry.setStatus(TimeEntryStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(timeEntryRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(entry));
        doThrow(new ForbiddenException("time-entry.approve-self"))
                .when(approvalPolicy).checkNotOwnRequest(userId, "time-entry.approve-self");

        assertThatThrownBy(() -> timeEntryService.reject(id, "reason"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void findPendingDelegatesToRepositoryWithSubmittedStatus() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(timeEntryRepository.findPending(organizationId, TimeEntryStatus.SUBMITTED, teamId))
                .thenReturn(List.of(entry));

        assertThat(timeEntryService.findPending(teamId)).containsExactly(entry);
    }

    @Test
    void restoreThrowsWhenEntryBelongsToAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(currentUser.userId()).thenReturn(userId);
        when(timeEntryRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeEntryService.restore(id))
                .isInstanceOf(NotFoundException.class);
    }

}
