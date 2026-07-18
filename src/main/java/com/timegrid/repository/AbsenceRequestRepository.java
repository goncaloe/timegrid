package com.timegrid.repository;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.enums.AbsenceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, UUID> {

    @EntityGraph(attributePaths = "user")
    List<AbsenceRequest> findByUserIdAndDeletedAtIsNullAndStartDateBetweenOrderByStartDateAsc(
            UUID userId, LocalDate from, LocalDate to);

    List<AbsenceRequest> findByUserIdAndDeletedAtIsNullAndStatusNotIn(UUID userId, List<AbsenceStatus> statuses);

    @Query("""
            SELECT ar 
            FROM AbsenceRequest ar
            WHERE ar.user.id = :userId
              AND ar.status = com.timegrid.enums.AbsenceStatus.APPROVED
              AND ar.deletedAt IS NULL
              AND ar.startDate <= :to
              AND ar.endDate >= :from
            """)
    List<AbsenceRequest> findApprovedOverlapping(UUID userId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = "user")
    Optional<AbsenceRequest> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<AbsenceRequest> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = "user")
    Optional<AbsenceRequest> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            SELECT ar
            FROM AbsenceRequest ar
            WHERE ar.organization.id = :organizationId
              AND ar.status = :status
              AND ar.deletedAt IS NULL
              AND (:teamId IS NULL OR ar.user.team.id = :teamId)
            ORDER BY ar.startDate ASC
            """)
    List<AbsenceRequest> findPending(UUID organizationId, AbsenceStatus status, UUID teamId);

    @Query("""
            SELECT ar FROM AbsenceRequest ar
            JOIN FETCH ar.user u
            LEFT JOIN u.team t
            WHERE ar.organization.id = :organizationId
              AND ar.deletedAt IS NULL
              AND ar.startDate <= :to
              AND ar.endDate >= :from
              AND (:userId IS NULL OR u.id = :userId)
              AND (:teamId IS NULL OR t.id = :teamId)
            ORDER BY u.fullName ASC, ar.startDate ASC
            """)
    List<AbsenceRequest> findForExport(UUID organizationId, LocalDate from, LocalDate to, UUID userId, UUID teamId);

}