package com.timegrid.repository;

import com.timegrid.entity.Organization;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    List<Organization> findByDeletedAtIsNull();

    Optional<Organization> findByIdAndDeletedAtIsNull(UUID id);

    List<Organization> findByDeletedAtIsNotNull();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

}
