package com.timegrid.repository;

import com.timegrid.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
