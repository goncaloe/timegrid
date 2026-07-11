package com.timegrid.service;

import com.timegrid.enums.WorkflowEntityType;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.repository.WorkflowTransitionRepository;
import java.util.List;
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

}