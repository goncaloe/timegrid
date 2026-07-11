package com.timegrid.service;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.Organization;
import com.timegrid.entity.User;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import com.timegrid.enums.WorkflowEntityType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ForbiddenException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import com.timegrid.service.ApprovalPolicy;
import com.timegrid.service.StatusHistoryService;
import com.timegrid.service.WorkflowService;
import java.time.LocalDate;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbsenceRequestServiceTest {

    @Mock
    private AbsenceRequestRepository absenceRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private ApprovalPolicy approvalPolicy;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private StatusHistoryService statusHistoryService;

    @InjectMocks
    private AbsenceRequestService absenceRequestService;

    private User userWithId(UUID id) {
        User user = new User();
        user.setOrganization(new Organization());
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void createRejectsWhenEndIsBeforeStart() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithId(userId)));

        assertThatThrownBy(() -> absenceRequestService.create(
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 5), AbsenceType.VACATION, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createRejectsOverlappingRequest() {
        UUID userId = UUID.randomUUID();
        AbsenceRequest existing = new AbsenceRequest();
        existing.setStartDate(LocalDate.of(2026, 9, 10));
        existing.setEndDate(LocalDate.of(2026, 9, 15));

        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithId(userId)));
        when(absenceRequestRepository.findByUserIdAndDeletedAtIsNullAndStatusNotIn(
                userId, List.of(AbsenceStatus.REJECTED, AbsenceStatus.CANCELLED)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> absenceRequestService.create(
                LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 20), AbsenceType.VACATION, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createSucceedsAndStartsAsDraft() {
        UUID userId = UUID.randomUUID();
        User user = userWithId(userId);

        when(currentUser.userId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(absenceRequestRepository.findByUserIdAndDeletedAtIsNullAndStatusNotIn(
                userId, List.of(AbsenceStatus.REJECTED, AbsenceStatus.CANCELLED)))
                .thenReturn(List.of());
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest created = absenceRequestService.create(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), AbsenceType.VACATION, "summer vacation");

        assertThat(created.getStatus()).isEqualTo(AbsenceStatus.DRAFT);
        assertThat(created.getOrganization()).isEqualTo(user.getOrganization());
    }

    @Test
    void updateRejectsWhenNotDraftOrRejected() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.SUBMITTED);

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> absenceRequestService.update(
                id, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), AbsenceType.VACATION, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateResetsRejectedRequestToDraft() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setUser(userWithId(userId));
        request.setStatus(AbsenceStatus.REJECTED);
        request.setRejectionReason("motivo antigo");

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));
        when(absenceRequestRepository.findByUserIdAndDeletedAtIsNullAndStatusNotIn(
                userId, List.of(AbsenceStatus.REJECTED, AbsenceStatus.CANCELLED)))
                .thenReturn(List.of());
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest updated = absenceRequestService.update(
                id, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5), AbsenceType.VACATION, null);

        assertThat(updated.getStatus()).isEqualTo(AbsenceStatus.DRAFT);
        assertThat(updated.getRejectionReason()).isNull();
    }

    @Test
    void deleteRejectsWhenNotDraftOrRejected() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.APPROVED);

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> absenceRequestService.delete(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void restoreClearsDeletedAt() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setDeletedAt(java.time.Instant.now());

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(request));
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest restored = absenceRequestService.restore(id);

        assertThat(restored.getDeletedAt()).isNull();
    }

    @Test
    void restoreThrowsWhenRequestBelongsToAnotherUser() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> absenceRequestService.restore(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void submitMovesDraftToSubmitted() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.DRAFT);

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest submitted = absenceRequestService.submit(id);

        assertThat(submitted.getStatus()).isEqualTo(AbsenceStatus.SUBMITTED);
    }

    @Test
    void submitRejectsWhenAlreadySubmitted() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.SUBMITTED);

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));
        doThrow(new BusinessRuleException("workflow.transition-not-allowed"))
                .when(workflowService).ensureAllowed(eq(WorkflowEntityType.ABSENCE), any(), any(), any());

        assertThatThrownBy(() -> absenceRequestService.submit(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void approveSetsApproverAndStatus() {
        UUID organizationId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setUser(userWithId(ownerId));
        request.setStatus(AbsenceStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(currentUser.userId()).thenReturn(approverId);
        when(absenceRequestRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(request));
        when(userRepository.getReferenceById(approverId)).thenReturn(userWithId(approverId));
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest approved = absenceRequestService.approve(id);

        assertThat(approved.getStatus()).isEqualTo(AbsenceStatus.APPROVED);
        assertThat(approved.getApprover().getId()).isEqualTo(approverId);
        assertThat(approved.getApprovedAt()).isNotNull();
    }

    @Test
    void approveRejectsOwnRequest() {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setUser(userWithId(userId));
        request.setStatus(AbsenceStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(absenceRequestRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(request));
        doThrow(new ForbiddenException("absence.approve-self"))
                .when(approvalPolicy).checkNotOwnRequest(userId, "absence.approve-self");

        assertThatThrownBy(() -> absenceRequestService.approve(id))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveRejectsWhenNotSubmitted() {
        UUID organizationId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setUser(userWithId(UUID.randomUUID()));
        request.setStatus(AbsenceStatus.DRAFT);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(absenceRequestRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(request));
        doThrow(new BusinessRuleException("workflow.transition-not-allowed"))
                .when(workflowService).ensureAllowed(eq(WorkflowEntityType.ABSENCE), any(), any(), any());

        assertThatThrownBy(() -> absenceRequestService.approve(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rejectSetsReasonAndStatus() {
        UUID organizationId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setUser(userWithId(ownerId));
        request.setStatus(AbsenceStatus.SUBMITTED);

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(absenceRequestRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId))
                .thenReturn(Optional.of(request));
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest rejected = absenceRequestService.reject(id, "there are not enough vacation days available");

        assertThat(rejected.getStatus()).isEqualTo(AbsenceStatus.REJECTED);
        assertThat(rejected.getRejectionReason()).isEqualTo("there are not enough vacation days available");
    }

    @Test
    void cancelSucceedsBeforeStartDate() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.APPROVED);
        request.setStartDate(LocalDate.now().plusDays(5));

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));
        when(absenceRequestRepository.save(any(AbsenceRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AbsenceRequest cancelled = absenceRequestService.cancel(id);

        assertThat(cancelled.getStatus()).isEqualTo(AbsenceStatus.CANCELLED);
    }

    @Test
    void cancelRejectsWhenStartDateAlreadyArrived() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.APPROVED);
        request.setStartDate(LocalDate.now());

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> absenceRequestService.cancel(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancelRejectsWhenNotSubmittedOrApproved() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();
        request.setStatus(AbsenceStatus.DRAFT);
        request.setStartDate(LocalDate.now().plusDays(5));

        when(currentUser.userId()).thenReturn(userId);
        when(absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)).thenReturn(Optional.of(request));
        doThrow(new BusinessRuleException("workflow.transition-not-allowed"))
                .when(workflowService).ensureAllowed(eq(WorkflowEntityType.ABSENCE), any(), any(), any());

        assertThatThrownBy(() -> absenceRequestService.cancel(id))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void findPendingDelegatesToRepositoryWithSubmittedStatus() {
        UUID organizationId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        AbsenceRequest request = new AbsenceRequest();

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(absenceRequestRepository.findPending(organizationId, AbsenceStatus.SUBMITTED, teamId))
                .thenReturn(List.of(request));

        assertThat(absenceRequestService.findPending(teamId)).containsExactly(request);
    }

}