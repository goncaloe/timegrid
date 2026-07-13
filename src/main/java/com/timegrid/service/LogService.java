package com.timegrid.service;

import com.timegrid.common.RequestOrigin;
import com.timegrid.entity.LogEvent;
import com.timegrid.entity.Organization;
import com.timegrid.enums.LogAction;
import com.timegrid.repository.LogEventRepository;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.security.CurrentUser;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEventRepository logEventRepository;
    private final RequestOrigin requestOrigin;
    private final OrganizationRepository organizationRepository;
    private final CurrentUser currentUser;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID organizationId, UUID userId, LogAction action, String targetType, UUID targetId,
            String details) {
        LogEvent event = new LogEvent();
        event.setOrganizationId(organizationId);
        event.setUserId(userId);
        event.setAction(action);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setDetails(truncate(details, 500));
        event.setIpAddress(truncate(requestOrigin.ipAddress(), 45));
        event.setUserAgent(truncate(requestOrigin.userAgent(), 255));
        logEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<LogEvent> search(UUID userId, LogAction action, LocalDate from, LocalDate to, Pageable pageable) {
        UUID organizationId = currentUser.organizationId();
        ZoneId zone = organizationRepository.findById(organizationId)
                .map(Organization::getTimezone)
                .map(ZoneId::of)
                .orElse(ZoneId.of("Europe/Lisbon"));

        Specification<LogEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), organizationId));
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay(zone).toInstant()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay(zone).toInstant()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return logEventRepository.findAll(spec, pageable);
    }

    private static String truncate(String value, int max) {
        return value != null && value.length() > max ? value.substring(0, max) : value;
    }

}