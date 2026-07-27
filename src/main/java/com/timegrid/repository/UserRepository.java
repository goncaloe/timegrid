package com.timegrid.repository;

import com.timegrid.entity.User;
import com.timegrid.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "roles")
    Page<User> findByOrganizationIdAndDeletedAtIsNull(UUID organizationId, Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = { "roles", "roles.permissions" })
    Optional<User> findForAuthById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsernameOrEmailIgnoreCase(String username, String email);

    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN u.roles r
            LEFT JOIN FETCH u.team
            WHERE u.organization.id = :organizationId
              AND r.name = :role
              AND u.deletedAt IS NULL
              AND u.status = com.timegrid.enums.UserStatus.ACTIVE
            """)
    List<User> findActiveByOrganizationIdAndRole(UUID organizationId, String role);

    @Query("SELECT t.id FROM User u JOIN u.team t WHERE u.id = :id")
    Optional<UUID> findTeamIdById(UUID id);

}