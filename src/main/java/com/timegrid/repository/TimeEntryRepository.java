package com.timegrid.repository;

import com.timegrid.entity.TimeEntry;
import com.timegrid.enums.TimeEntryStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

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
            SELECT te FROM TimeEntry te
            WHERE te.organization.id = :organizationId
              AND te.status = :status
              AND te.deletedAt IS NULL
              AND (:teamId is null or te.user.team.id = :teamId)
            ORDER BY te.workDate ASC, te.startTime ASC
            """)
    List<TimeEntry> findPending(UUID organizationId, TimeEntryStatus status, UUID teamId);

}
