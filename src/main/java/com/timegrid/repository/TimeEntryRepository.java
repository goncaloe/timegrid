package com.timegrid.repository;

import com.timegrid.entity.TimeEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByUserIdAndWorkDateBetweenAndDeletedAtIsNullOrderByWorkDateAscStartTimeAsc(
            UUID userId, LocalDate from, LocalDate to);

}
