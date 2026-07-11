package com.timegrid.entity;

import com.timegrid.entity.BaseEntity;
import com.timegrid.enums.WorkflowEntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workflow_transitions")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowTransition extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private WorkflowEntityType entityType;

    @Column(name = "from_status", nullable = false)
    private String fromStatus;

    @Column(name = "to_status", nullable = false)
    private String toStatus;

    @Column(nullable = false)
    private String role;

}