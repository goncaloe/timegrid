package com.timegrid.service;

import com.timegrid.entity.Holiday;
import com.timegrid.exception.ConflictException;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.HolidayRepository;
import com.timegrid.security.CurrentUser;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<Holiday> findForYear(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return holidayRepository.findByOrganizationIdAndDateBetweenOrderByDateAsc(currentUser.organizationId(), from,
                to);
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

}