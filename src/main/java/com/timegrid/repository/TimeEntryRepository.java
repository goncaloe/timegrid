package com.timegrid.repository;

import com.timegrid.entity.TimeEntry;
import com.timegrid.enums.TimeEntryStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    @EntityGraph(attributePaths = "user")
    List<TimeEntry> findByUserIdAndWorkDateBetweenAndDeletedAtIsNullOrderByWorkDateAscStartTimeAsc(
            UUID userId, LocalDate from, LocalDate to);

    List<TimeEntry> findByUserIdAndWorkDateAndDeletedAtIsNull(UUID userId, LocalDate workDate);

    @EntityGraph(attributePaths = "user")
    Optional<TimeEntry> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<TimeEntry> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    @EntityGraph(attributePaths = "user")
    Optional<TimeEntry> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            SELECT te 
            FROM TimeEntry te
            WHERE te.organization.id = :organizationId
              AND te.status = :status
              AND te.deletedAt IS NULL
              AND (:teamId IS NULL OR te.user.team.id = :teamId)
            ORDER BY te.workDate ASC, te.startTime ASC
            """)
    List<TimeEntry> findPending(UUID organizationId, TimeEntryStatus status, UUID teamId);

    @Query("""
            SELECT te 
            FROM TimeEntry te
            JOIN FETCH te.user u
            LEFT JOIN u.team t
            WHERE te.organization.id = :organizationId
              AND te.deletedAt IS NULL
              AND te.workDate BETWEEN :from AND :to
              AND (:userId IS NULL OR u.id = :userId)
              AND (:teamId IS NULL OR t.id = :teamId)
            ORDER BY u.fullName ASC, te.workDate ASC, te.startTime ASC
            """)
    List<TimeEntry> findForExport(UUID organizationId, LocalDate from, LocalDate to, UUID userId, UUID teamId);

}