package com.timegrid.service;

import com.timegrid.entity.WorkflowTransition;
import com.timegrid.enums.WorkflowEntityType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.WorkflowTransitionRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowTransitionRepository workflowTransitionRepository;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void ensureAllowedDoesNothingWhenTransitionExists() {
        when(workflowTransitionRepository.existsByEntityTypeAndFromStatusAndToStatusAndRoleIn(
                WorkflowEntityType.TIME_ENTRY, "DRAFT", "SUBMITTED", List.of("EMPLOYEE")))
                .thenReturn(true);

        workflowService.ensureAllowed(WorkflowEntityType.TIME_ENTRY, "DRAFT", "SUBMITTED", List.of("EMPLOYEE"));
    }

    @Test
    void ensureAllowedRejectsWhenTransitionMissing() {
        when(workflowTransitionRepository.existsByEntityTypeAndFromStatusAndToStatusAndRoleIn(
                WorkflowEntityType.TIME_ENTRY, "APPROVED", "SUBMITTED", List.of("EMPLOYEE")))
                .thenReturn(false);

        assertThatThrownBy(() -> workflowService.ensureAllowed(
                WorkflowEntityType.TIME_ENTRY, "APPROVED", "SUBMITTED", List.of("EMPLOYEE")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allowedTargetsDelegatesToRepository() {
        when(workflowTransitionRepository.findAllowedTargets(
                WorkflowEntityType.ABSENCE, "SUBMITTED", List.of("MANAGER")))
                .thenReturn(List.of("APPROVED", "REJECTED"));

        assertThat(workflowService.allowedTargets(WorkflowEntityType.ABSENCE, "SUBMITTED", List.of("MANAGER")))
                .containsExactly("APPROVED", "REJECTED");
    }

    @Test
    void createRejectsUnknownStatus() {
        assertThatThrownBy(
                () -> workflowService.create(WorkflowEntityType.TIME_ENTRY, "SUBMITED", "APPROVED", "MANAGER"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void createRejectsDuplicateTransition() {
        when(workflowTransitionRepository.existsByEntityTypeAndFromStatusAndToStatusAndRole(
                WorkflowEntityType.TIME_ENTRY, "DRAFT", "SUBMITTED", "EMPLOYEE"))
                .thenReturn(true);

        assertThatThrownBy(
                () -> workflowService.create(WorkflowEntityType.TIME_ENTRY, "DRAFT", "SUBMITTED", "EMPLOYEE"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createSavesNewTransition() {
        when(workflowTransitionRepository.existsByEntityTypeAndFromStatusAndToStatusAndRole(
                WorkflowEntityType.ABSENCE, "DRAFT", "SUBMITTED", "MANAGER"))
                .thenReturn(false);
        when(workflowTransitionRepository.save(any(WorkflowTransition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowTransition created = workflowService.create(WorkflowEntityType.ABSENCE, "DRAFT", "SUBMITTED",
                "MANAGER");

        assertThat(created.getEntityType()).isEqualTo(WorkflowEntityType.ABSENCE);
        assertThat(created.getFromStatus()).isEqualTo("DRAFT");
        assertThat(created.getToStatus()).isEqualTo("SUBMITTED");
        assertThat(created.getRole()).isEqualTo("MANAGER");
    }

    @Test
    void deleteRejectsWhenTransitionMissing() {
        UUID id = UUID.randomUUID();
        when(workflowTransitionRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> workflowService.delete(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteRemovesExistingTransition() {
        UUID id = UUID.randomUUID();
        when(workflowTransitionRepository.existsById(id)).thenReturn(true);

        workflowService.delete(id);
    }

}