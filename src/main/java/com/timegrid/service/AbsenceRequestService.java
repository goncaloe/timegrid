package com.timegrid.service;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.User;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import com.timegrid.exception.NotFoundException;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.UserRepository;
import com.timegrid.security.CurrentUser;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbsenceRequestService {

    private final AbsenceRequestRepository absenceRequestRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<AbsenceRequest> findOwn(Integer year) {
        int requestedYear = year != null ? year : LocalDate.now().getYear();
        return absenceRequestRepository.findByUserIdAndDeletedAtIsNullAndStartDateBetweenOrderByStartDateAsc(
                currentUser.userId(), LocalDate.of(requestedYear, 1, 1), LocalDate.of(requestedYear, 12, 31));
    }

    @Transactional
    public AbsenceRequest create(LocalDate startDate, LocalDate endDate, AbsenceType type, String notes) {
        User user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> new NotFoundException("User not found.", currentUser.userId()));

        AbsenceRequest request = new AbsenceRequest();
        request.setUser(user);
        request.setOrganization(user.getOrganization());
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setType(type);
        request.setNotes(notes);
        request.setStatus(AbsenceStatus.DRAFT);

        return absenceRequestRepository.save(request);
    }

}
