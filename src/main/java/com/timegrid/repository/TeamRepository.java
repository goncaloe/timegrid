package com.timegrid.repository;

import com.timegrid.entity.Team;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    Optional<Team> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Optional<Team> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndNameAndDeletedAtIsNull(UUID organizationId, String name);

    boolean existsByOrganizationIdAndNameAndDeletedAtIsNullAndIdNot(UUID organizationId, String name, UUID id);

}
