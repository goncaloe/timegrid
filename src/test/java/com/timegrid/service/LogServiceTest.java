package com.timegrid.service;

import com.timegrid.common.RequestOrigin;
import com.timegrid.entity.LogEvent;
import com.timegrid.enums.LogAction;
import com.timegrid.repository.LogEventRepository;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.security.CurrentUser;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogEventRepository logEventRepository;

    @Mock
    private RequestOrigin requestOrigin;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private LogService logService;

    @Test
    void recordGuardaOrganizacaoUtilizadorEOrigemDoPedido() {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(requestOrigin.ipAddress()).thenReturn("10.0.0.7");
        when(requestOrigin.userAgent()).thenReturn("curl/8.0");

        logService.record(organizationId, userId, LogAction.USER_CREATED, "USER", targetId, "ines.moura");

        ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
        verify(logEventRepository).save(captor.capture());
        LogEvent saved = captor.getValue();
        assertThat(saved.getOrganizationId()).isEqualTo(organizationId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getAction()).isEqualTo(LogAction.USER_CREATED);
        assertThat(saved.getTargetType()).isEqualTo("USER");
        assertThat(saved.getTargetId()).isEqualTo(targetId);
        assertThat(saved.getDetails()).isEqualTo("ines.moura");
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.7");
        assertThat(saved.getUserAgent()).isEqualTo("curl/8.0");
    }

    @Test
    void recordCortaDetalhesEUserAgentDemasiadoLongos() {
        when(requestOrigin.userAgent()).thenReturn("x".repeat(300));

        logService.record(null, null, LogAction.LOGIN_FAILED, null, null, "y".repeat(600));

        ArgumentCaptor<LogEvent> captor = ArgumentCaptor.forClass(LogEvent.class);
        verify(logEventRepository).save(captor.capture());
        LogEvent saved = captor.getValue();
        assertThat(saved.getOrganizationId()).isNull();
        assertThat(saved.getDetails()).hasSize(500);
        assertThat(saved.getUserAgent()).hasSize(255);
    }

    @Test
    void searchFiltraSemprePelaOrganizacaoDoUtilizadorAtual() {
        UUID organizationId = UUID.randomUUID();
        when(currentUser.organizationId()).thenReturn(organizationId);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.empty());
        when(logEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        logService.search(null, null, null, null, Pageable.unpaged());

        verify(logEventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

}