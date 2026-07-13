package com.timegrid.service;

import com.timegrid.common.RequestOrigin;
import com.timegrid.entity.LogEvent;
import com.timegrid.enums.LogAction;
import com.timegrid.repository.LogEventRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogEventRepository logEventRepository;
    private final RequestOrigin requestOrigin;

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

    private static String truncate(String value, int max) {
        return value != null && value.length() > max ? value.substring(0, max) : value;
    }

}