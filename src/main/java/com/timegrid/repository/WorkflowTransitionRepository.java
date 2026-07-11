package com.timegrid.repository;

import com.timegrid.entity.WorkflowTransition;
import com.timegrid.enums.WorkflowEntityType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {

    boolean existsByEntityTypeAndFromStatusAndToStatusAndRoleIn(
            WorkflowEntityType entityType, String fromStatus, String toStatus, Collection<String> roles);

    boolean existsByEntityTypeAndFromStatusAndToStatusAndRole(
            WorkflowEntityType entityType, String fromStatus, String toStatus, String role);

    @Query("""
            SELECT DISTINCT wt.toStatus
            FROM WorkflowTransition wt
            WHERE wt.entityType = :entityType
              AND wt.fromStatus = :fromStatus
              AND wt.role IN :roles
            """)
    List<String> findAllowedTargets(WorkflowEntityType entityType, String fromStatus, Collection<String> roles);

    List<WorkflowTransition> findAllByOrderByEntityTypeAscFromStatusAscToStatusAsc();

}