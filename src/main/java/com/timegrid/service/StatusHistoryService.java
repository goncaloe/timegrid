package com.timegrid.service;

import com.timegrid.entity.StatusChange;
import com.timegrid.enums.WorkflowEntityType;
import com.timegrid.repository.StatusChangeRepository;
import com.timegrid.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatusHistoryService {

    private final StatusChangeRepository statusChangeRepository;
    private final CurrentUser currentUser;

    @Transactional
    public void record(WorkflowEntityType entityType, UUID entityId, String fromStatus, String toStatus,
            String comment) {
        StatusChange change = new StatusChange();
        change.setEntityType(entityType);
        change.setEntityId(entityId);
        change.setFromStatus(fromStatus);
        change.setToStatus(toStatus);
        change.setChangedBy(currentUser.userId());
        change.setComment(comment);
        statusChangeRepository.save(change);
    }

    @Transactional(readOnly = true)
    public List<StatusChange> findHistory(WorkflowEntityType entityType, UUID entityId) {
        return statusChangeRepository.findByEntityTypeAndEntityIdOrderByChangedAtAsc(entityType, entityId);
    }

}