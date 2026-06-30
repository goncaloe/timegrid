package com.timegrid.repository;

import com.timegrid.entity.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
