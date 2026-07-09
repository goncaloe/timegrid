package com.timegrid.repository;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.enums.AbsenceStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, UUID> {

    List<AbsenceRequest> findByUserIdAndDeletedAtIsNullAndStartDateBetweenOrderByStartDateAsc(
            UUID userId, LocalDate from, LocalDate to);

    List<AbsenceRequest> findByUserIdAndDeletedAtIsNullAndStatusNotIn(UUID userId, List<AbsenceStatus> statuses);

    Optional<AbsenceRequest> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<AbsenceRequest> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    Optional<AbsenceRequest> findByIdAndUserId(UUID id, UUID userId);

}
