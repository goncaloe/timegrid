package com.timegrid.entity;

import com.timegrid.enums.OrganizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
public class Organization extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    private Instant deletedAt;

}
