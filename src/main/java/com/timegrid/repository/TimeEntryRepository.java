package com.timegrid.repository;

import com.timegrid.entity.TimeEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByUserIdAndWorkDateBetweenAndDeletedAtIsNullOrderByWorkDateAscStartTimeAsc(
            UUID userId, LocalDate from, LocalDate to);

    List<TimeEntry> findByUserIdAndWorkDateAndDeletedAtIsNull(UUID userId, LocalDate workDate);

    Optional<TimeEntry> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<TimeEntry> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Optional<TimeEntry> findByIdAndUserId(UUID id, UUID userId);
}
