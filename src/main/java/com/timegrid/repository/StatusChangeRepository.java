package com.timegrid.repository;

import com.timegrid.entity.StatusChange;
import com.timegrid.enums.WorkflowEntityType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusChangeRepository extends JpaRepository<StatusChange, UUID> {

    List<StatusChange> findByEntityTypeAndEntityIdOrderByChangedAtAsc(WorkflowEntityType entityType, UUID entityId);

}