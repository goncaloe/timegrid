package com.timegrid.repository;

import com.timegrid.entity.AbsenceRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, UUID> {

    List<AbsenceRequest> findByUserIdAndDeletedAtIsNullAndStartDateBetweenOrderByStartDateAsc(
            UUID userId, LocalDate from, LocalDate to);

}
