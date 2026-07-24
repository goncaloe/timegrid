package com.timegrid.service;

import com.timegrid.common.NationalHoliday;
import com.timegrid.common.PortugueseHolidays;
import com.timegrid.common.WorkingDays;
import com.timegrid.dto.HolidayResponse;
import com.timegrid.entity.Holiday;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.HolidayRepository;
import com.timegrid.security.CurrentUser;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private static final String CACHE_NAME = "organizationHolidays";

    private final HolidayRepository holidayRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<HolidayResponse> findForYear(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        List<HolidayResponse> result = new ArrayList<>();
        for (NationalHoliday holiday : PortugueseHolidays.of(year)) {
            result.add(HolidayResponse.national(holiday));
        }
        for (Holiday holiday : holidayRepository.findByOrganizationIdAndDateBetweenOrderByDateAsc(
                currentUser.organizationId(), from, to)) {
            result.add(HolidayResponse.from(holiday));
        }
        result.sort(Comparator.comparing(HolidayResponse::date));
        return result;
    }

    @Transactional
    public Holiday create(LocalDate date, String name) {
        if (holidayRepository.existsByOrganizationIdAndDate(currentUser.organizationId(), date)) {
            throw new ConflictException("holiday.date.taken");
        }

        Holiday holiday = new Holiday();
        holiday.setOrganizationId(currentUser.organizationId());
        holiday.setDate(date);
        holiday.setName(name);
        return holidayRepository.save(holiday);
    }

    @Transactional
    public void delete(UUID id) {
        Holiday holiday = holidayRepository.findByIdAndOrganizationId(id, currentUser.organizationId())
                .orElseThrow(() -> new NotFoundException("holiday.not-found", id));
        holidayRepository.delete(holiday);
    }

    // feriados nacionais + da organização, para descontar dias úteis (banco de horas, ausências)
    @Cacheable(CACHE_NAME)
    @Transactional(readOnly = true)
    public Set<LocalDate> datesBetween(UUID organizationId, LocalDate from, LocalDate to) {
        Set<LocalDate> dates = new HashSet<>();
        for (int year = from.getYear(); year <= to.getYear(); year++) {
            for (NationalHoliday holiday : PortugueseHolidays.of(year)) {
                if (!holiday.date().isBefore(from) && !holiday.date().isAfter(to)) {
                    dates.add(holiday.date());
                }
            }
        }
        for (Holiday holiday : holidayRepository.findByOrganizationIdAndDateBetweenOrderByDateAsc(organizationId, from, to)) {
            dates.add(holiday.getDate());
        }
        return dates;
    }

    @Transactional(readOnly = true)
    public long workingDays(UUID organizationId, LocalDate from, LocalDate to) {
        return WorkingDays.between(from, to, datesBetween(organizationId, from, to));
    }

}