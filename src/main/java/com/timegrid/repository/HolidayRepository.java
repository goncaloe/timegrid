package com.timegrid.repository;

import com.timegrid.entity.Holiday;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    List<Holiday> findByOrganizationIdAndDateBetweenOrderByDateAsc(UUID organizationId, LocalDate from, LocalDate to);

    Optional<Holiday> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndDate(UUID organizationId, LocalDate date);

}