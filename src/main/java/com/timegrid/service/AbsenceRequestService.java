package com.timegrid.service;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.StatusChange;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbsenceRequestService {

    private final AbsenceRequestRepository absenceRequestRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final ApprovalPolicy approvalPolicy;
    private final WorkflowService workflowService;
    private final StatusHistoryService statusHistoryService;

    @Transactional(readOnly = true)
    public List<AbsenceRequest> findOwn(Integer year) {
        int requestedYear = year != null ? year : LocalDate.now().getYear();
        return absenceRequestRepository.findByUserIdAndDeletedAtIsNullAndStartDateBetweenOrderByStartDateAsc(
                currentUser.userId(), LocalDate.of(requestedYear, 1, 1), LocalDate.of(requestedYear, 12, 31));
    }

    @Transactional(readOnly = true)
    public List<AbsenceRequest> findPending(UUID teamId) {
        return absenceRequestRepository.findPending(currentUser.organizationId(), AbsenceStatus.SUBMITTED, teamId);
    }

    @Transactional(readOnly = true)
    public List<StatusChange> history(UUID id) {
        AbsenceRequest request = absenceRequestRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("absence.not-found", id));
        boolean isOwner = request.getUser().getId().equals(currentUser.userId());
        if (!isOwner && !currentUser.hasRole("MANAGER") && !currentUser.hasRole("ADMIN")) {
            throw new ForbiddenException("absence.history-forbidden");
        }
        return statusHistoryService.findHistory(WorkflowEntityType.ABSENCE, id);
    }

    @Transactional
    public AbsenceRequest create(LocalDate startDate, LocalDate endDate, AbsenceType type, String notes) {
        User user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new NotFoundException("user.not-found", currentUser.userId()));

        validateDates(startDate, endDate);
        checkOverlap(user.getId(), startDate, endDate, null);

        AbsenceRequest request = new AbsenceRequest();
        request.setUser(user);
        request.setOrganization(user.getOrganization());
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setType(type);
        request.setNotes(notes);
        request.setStatus(AbsenceStatus.DRAFT);

        AbsenceRequest saved = absenceRequestRepository.save(request);
        statusHistoryService.record(WorkflowEntityType.ABSENCE, saved.getId(), null, AbsenceStatus.DRAFT.name(), null);
        return saved;
    }

    @Transactional
    public AbsenceRequest submit(UUID id) {
        AbsenceRequest request = absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("absence.not-found", id));
        AbsenceStatus from = request.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.ABSENCE, from.name(), AbsenceStatus.SUBMITTED.name(),
                currentUser.roles());
        request.setStatus(AbsenceStatus.SUBMITTED);
        request.setRejectionReason(null);
        AbsenceRequest saved = absenceRequestRepository.save(request);
        statusHistoryService.record(WorkflowEntityType.ABSENCE, saved.getId(), from.name(),
                AbsenceStatus.SUBMITTED.name(), null);
        return saved;
    }

    @Transactional
    public AbsenceRequest approve(UUID id) {
        AbsenceRequest request = findForApproval(id);
        AbsenceStatus from = request.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.ABSENCE, from.name(), AbsenceStatus.APPROVED.name(),
                currentUser.roles());
        request.setStatus(AbsenceStatus.APPROVED);
        request.setApprover(userRepository.getReferenceById(currentUser.userId()));
        request.setApprovedAt(Instant.now());
        request.setRejectionReason(null);
        AbsenceRequest saved = absenceRequestRepository.save(request);
        statusHistoryService.record(WorkflowEntityType.ABSENCE, saved.getId(), from.name(),
                AbsenceStatus.APPROVED.name(), null);
        return saved;
    }

    @Transactional
    public AbsenceRequest reject(UUID id, String reason) {
        AbsenceRequest request = findForApproval(id);
        AbsenceStatus from = request.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.ABSENCE, from.name(), AbsenceStatus.REJECTED.name(),
                currentUser.roles());
        request.setStatus(AbsenceStatus.REJECTED);
        request.setRejectionReason(reason);
        AbsenceRequest saved = absenceRequestRepository.save(request);
        statusHistoryService.record(WorkflowEntityType.ABSENCE, saved.getId(), from.name(),
                AbsenceStatus.REJECTED.name(), reason);
        return saved;
    }

    @Transactional
    public AbsenceRequest update(UUID id, LocalDate startDate, LocalDate endDate, AbsenceType type, String notes) {
        AbsenceRequest request = findEditable(id);

        validateDates(startDate, endDate);
        checkOverlap(request.getUser().getId(), startDate, endDate, id);

        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setType(type);
        request.setNotes(notes);
        if (request.getStatus() == AbsenceStatus.REJECTED) {
            request.setStatus(AbsenceStatus.DRAFT);
            request.setRejectionReason(null);
        }

        return absenceRequestRepository.save(request);
    }

    @Transactional
    public void delete(UUID id) {
        AbsenceRequest request = findEditable(id);
        request.setDeletedAt(Instant.now());
    }

    @Transactional
    public AbsenceRequest restore(UUID id) {
        AbsenceRequest request = absenceRequestRepository.findByIdAndUserId(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("absence.not-found", id));
        request.setDeletedAt(null);
        return absenceRequestRepository.save(request);
    }

    @Transactional
    public AbsenceRequest cancel(UUID id) {
        AbsenceRequest request = absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("absence.not-found", id));
        AbsenceStatus from = request.getStatus();
        workflowService.ensureAllowed(WorkflowEntityType.ABSENCE, from.name(), AbsenceStatus.CANCELLED.name(),
                currentUser.roles());
        if (!request.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("absence.cancel-too-late");
        }
        request.setStatus(AbsenceStatus.CANCELLED);
        AbsenceRequest saved = absenceRequestRepository.save(request);
        statusHistoryService.record(WorkflowEntityType.ABSENCE, saved.getId(), from.name(),
                AbsenceStatus.CANCELLED.name(), null);
        return saved;
    }

    private AbsenceRequest findForApproval(UUID id) {
        AbsenceRequest request = absenceRequestRepository
                .findByIdAndOrganizationIdAndDeletedAtIsNull(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("absence.not-found", id));
        approvalPolicy.checkNotOwnRequest(request.getUser().getId(), "absence.approve-self");
        return request;
    }

    private AbsenceRequest findEditable(UUID id) {
        AbsenceRequest request = absenceRequestRepository.findByIdAndUserIdAndDeletedAtIsNull(id, currentUser.userId())
                .orElseThrow(() -> new NotFoundException("absence.not-found", id));
        if (request.getStatus() != AbsenceStatus.DRAFT && request.getStatus() != AbsenceStatus.REJECTED) {
            throw new BusinessRuleException("absence.not-editable");
        }
        return request;
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessRuleException("absence.invalid-dates");
        }
    }

    private void checkOverlap(UUID userId, LocalDate startDate, LocalDate endDate, UUID excludeId) {
        boolean overlaps = absenceRequestRepository
                .findByUserIdAndDeletedAtIsNullAndStatusNotIn(userId,
                        List.of(AbsenceStatus.REJECTED, AbsenceStatus.CANCELLED))
                .stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .anyMatch(existing -> !existing.getEndDate().isBefore(startDate)
                        && !existing.getStartDate().isAfter(endDate));
        if (overlaps) {
            throw new BusinessRuleException("absence.overlap");
        }
    }

}