package com.timegrid.service;

import com.timegrid.entity.WorkflowTransition;
import com.timegrid.enums.WorkflowEntityType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.WorkflowTransitionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowTransitionRepository workflowTransitionRepository;

    @Transactional(readOnly = true)
    public void ensureAllowed(WorkflowEntityType entityType, String fromStatus, String toStatus, List<String> roles) {
        boolean allowed = workflowTransitionRepository
                .existsByEntityTypeAndFromStatusAndToStatusAndRoleIn(entityType, fromStatus, toStatus, roles);
        if (!allowed) {
            throw new BusinessRuleException("workflow.transition-not-allowed", fromStatus, toStatus);
        }
    }

    @Transactional(readOnly = true)
    public List<String> allowedTargets(WorkflowEntityType entityType, String fromStatus, List<String> roles) {
        return workflowTransitionRepository.findAllowedTargets(entityType, fromStatus, roles);
    }

    @Transactional(readOnly = true)
    public List<WorkflowTransition> findAll() {
        return workflowTransitionRepository.findAllByOrderByEntityTypeAscFromStatusAscToStatusAsc();
    }

    @Transactional
    public WorkflowTransition create(WorkflowEntityType entityType, String fromStatus, String toStatus, String role) {
        if (workflowTransitionRepository.existsByEntityTypeAndFromStatusAndToStatusAndRole(
                entityType, fromStatus, toStatus, role)) {
            throw new ConflictException("workflow.transition-exists");
        }
        WorkflowTransition transition = new WorkflowTransition();
        transition.setEntityType(entityType);
        transition.setFromStatus(fromStatus);
        transition.setToStatus(toStatus);
        transition.setRole(role);
        return workflowTransitionRepository.save(transition);
    }

    @Transactional
    public void delete(UUID id) {
        if (!workflowTransitionRepository.existsById(id)) {
            throw new NotFoundException("workflow.transition-not-found", id);
        }
        workflowTransitionRepository.deleteById(id);
    }

}