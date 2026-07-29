package com.timegrid.service;

import com.timegrid.common.PortugueseHolidays;
import com.timegrid.dto.HolidayResponse;
import com.timegrid.entity.Holiday;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.HolidayRepository;
import com.timegrid.security.CurrentUser;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private HolidayService holidayService;

    @Test
    void findForYearJuntaNacionaisEDaOrganizacaoOrdenadosPorData() {
        UUID organizationId = UUID.randomUUID();
        Holiday orgHoliday = new Holiday();
        orgHoliday.setDate(LocalDate.of(2026, 6, 13));
        orgHoliday.setName("Santo António");

        when(currentUser.organizationId()).thenReturn(organizationId);
        when(holidayRepository.findByOrganizationIdAndDateBetweenOrderByDateAsc(
                organizationId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(orgHoliday));

        List<HolidayResponse> result = holidayService.findForYear(2026);

        assertThat(result).hasSize(PortugueseHolidays.of(2026).size() + 1);
        assertThat(result).isSortedAccordingTo((a, b) -> a.date().compareTo(b.date()));
        assertThat(result).anySatisfy(response -> {
            assertThat(response.name()).isEqualTo("Santo António");
            assertThat(response.national()).isFalse();
        });
    }

    @Test
    void createRejeitaDataJaOcupada() {
        UUID organizationId = UUID.randomUUID();
        when(currentUser.organizationId()).thenReturn(organizationId);
        when(holidayRepository.existsByOrganizationIdAndDate(organizationId, LocalDate.of(2026, 6, 13)))
                .thenReturn(true);

        assertThatThrownBy(() -> holidayService.create(LocalDate.of(2026, 6, 13), "Santo António"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteRejeitaFeriadoDeOutraOrganizacao() {
        UUID organizationId = UUID.randomUUID();
        UUID holidayId = UUID.randomUUID();
        when(currentUser.organizationId()).thenReturn(organizationId);
        when(holidayRepository.findByIdAndOrganizationId(holidayId, organizationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> holidayService.delete(holidayId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void datesBetweenIncluiFeriadoNacionalEDaOrganizacao() {
        UUID organizationId = UUID.randomUUID();
        Holiday orgHoliday = new Holiday();
        orgHoliday.setDate(LocalDate.of(2026, 1, 2));

        when(holidayRepository.findByOrganizationIdAndDateBetweenOrderByDateAsc(
                any(), any(), any())).thenReturn(List.of(orgHoliday));

        Set<LocalDate> dates = holidayService.datesBetween(organizationId, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 2));

        assertThat(dates).contains(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
    }

    @Test
    void workingDaysDescontaFeriadosEFinsDeSemana() {
        UUID organizationId = UUID.randomUUID();
        when(holidayRepository.findByOrganizationIdAndDateBetweenOrderByDateAsc(any(), any(), any()))
                .thenReturn(List.of());

        // 2026-01-01 é quinta-feira (Ano Novo, feriado nacional) e 2026-01-02 é sexta-feira normal
        long days = holidayService.workingDays(organizationId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));

        assertThat(days).isEqualTo(1);
    }

}