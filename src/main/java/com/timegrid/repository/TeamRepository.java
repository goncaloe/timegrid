package com.timegrid.repository;

import com.timegrid.entity.Team;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByOrganizationId(UUID organizationId);

    List<Team> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    Optional<Team> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByOrganizationIdAndNameAndDeletedAtIsNull(UUID organizationId, String name);

    boolean existsByOrganizationIdAndNameAndDeletedAtIsNullAndIdNot(UUID organizationId, String name, UUID id);

}
