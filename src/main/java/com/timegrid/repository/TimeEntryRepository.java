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

    // nativa por causa da diferença entre horas (time - time); inclui utilizadores sem registos no período
    @Query(value = """
            SELECT u.id AS userId,
                   u.full_name AS fullName,
                   t.name AS teamName,
                   COUNT(te.id) AS entries,
                   COALESCE(SUM(EXTRACT(EPOCH FROM (te.end_time - te.start_time)) / 60 - te.break_minutes), 0) AS minutes
            FROM users u
            LEFT JOIN teams t ON t.id = u.team_id
            LEFT JOIN time_entries te ON te.user_id = u.id
                  AND te.status = 'APPROVED'
                  AND te.deleted_at IS NULL
                  AND te.work_date BETWEEN :from AND :to
            WHERE u.organization_id = :organizationId
              AND u.deleted_at IS NULL
            GROUP BY u.id, u.full_name, t.name
            ORDER BY u.full_name
            """, nativeQuery = true)
    List<UserTimeTotals> sumApprovedByUser(UUID organizationId, LocalDate from, LocalDate to);

}