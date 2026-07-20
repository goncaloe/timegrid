package com.timegrid.seed;

import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.Holiday;
import com.timegrid.entity.Organization;
import com.timegrid.entity.Team;
import com.timegrid.entity.TimeEntry;
import com.timegrid.entity.Role;
import com.timegrid.entity.User;
import com.timegrid.enums.AbsenceStatus;
import com.timegrid.enums.AbsenceType;
import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.HolidayRepository;
import com.timegrid.repository.OrganizationRepository;
import com.timegrid.repository.RoleRepository;
import com.timegrid.repository.TeamRepository;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataLoader implements ApplicationRunner {

    private static final String ADMIN_PASSWORD = "timegrid2026";
    private static final String MANAGER_PASSWORD = "gestor123";
    private static final String EMPLOYEE_PASSWORD = "equipa2026";

    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HolidayRepository holidayRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final AbsenceRequestRepository absenceRequestRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (organizationRepository.count() > 0) {
            return;
        }

        Organization tejoDigital = createOrganization("Tejo Digital", "Europe/Lisbon");
        Organization norteSoftware = createOrganization("Norte Software", "Europe/Lisbon");
        Organization atlanticoLogistica = createOrganization("Atlântico Logística", "Atlantic/Azores");

        Team itTejo = createTeam(tejoDigital, "IT");
        Team devOpsTejo = createTeam(tejoDigital, "DevOps");
        Team productTejo = createTeam(tejoDigital, "Product");
        Team marketingTejo = createTeam(tejoDigital, "Marketing");
        Team financeTejo = createTeam(tejoDigital, "Finance");

        Team backendNorte = createTeam(norteSoftware, "Backend");
        Team frontendNorte = createTeam(norteSoftware, "Frontend");

        Team operacoesAtlantico = createTeam(atlanticoLogistica, "Operações");
        Team armazemAtlantico = createTeam(atlanticoLogistica, "Armazém");
        createTeam(atlanticoLogistica, "Recursos Humanos");

        createUser("goncalo.esteves", "goncalo.esteves@tejodigital.pt", "Gonçalo Esteves",
                ADMIN_PASSWORD, "ADMIN", tejoDigital, null);
        User tiagoFigueiredo = createUser("tiago.figueiredo", "tiago.figueiredo@tejodigital.pt", "Tiago Figueiredo",
                MANAGER_PASSWORD, "MANAGER", tejoDigital, itTejo);
        User inesMoura = createUser("ines.moura", "ines.moura@tejodigital.pt", "Inês Moura",
                EMPLOYEE_PASSWORD, "EMPLOYEE", tejoDigital, itTejo);
        createUser("miguel.rodrigues", "miguel.rodrigues@tejodigital.pt", "Miguel Rodrigues",
                EMPLOYEE_PASSWORD, "EMPLOYEE", tejoDigital, itTejo);
        createUser("andre.santos", "andre.santos@tejodigital.pt", "André Santos",
                EMPLOYEE_PASSWORD, "EMPLOYEE", tejoDigital, devOpsTejo);
        createUser("beatriz.lopes", "beatriz.lopes@tejodigital.pt", "Beatriz Lopes",
                EMPLOYEE_PASSWORD, "EMPLOYEE", tejoDigital, productTejo);
        createUser("mariana.silva", "mariana.silva@tejodigital.pt", "Mariana Silva",
                EMPLOYEE_PASSWORD, "MANAGER", tejoDigital, marketingTejo);
        createUser("rui.fernandes", "rui.fernandes@tejodigital.pt", "Rui Fernandes",
                EMPLOYEE_PASSWORD, "EMPLOYEE", tejoDigital, marketingTejo);
        createUser("catarina.sousa", "catarina.sousa@tejodigital.pt", "Catarina Sousa",
                EMPLOYEE_PASSWORD, "EMPLOYEE", tejoDigital, financeTejo);

        createUser("joao.pereira", "joao.pereira@nortesoftware.pt", "João Pereira",
                ADMIN_PASSWORD, "ADMIN", norteSoftware, null);
        createUser("marta.ribeiro", "marta.ribeiro@nortesoftware.pt", "Marta Ribeiro",
                MANAGER_PASSWORD, "MANAGER", norteSoftware, backendNorte);
        createUser("nuno.almeida", "nuno.almeida@nortesoftware.pt", "Nuno Almeida",
                EMPLOYEE_PASSWORD, "EMPLOYEE", norteSoftware, frontendNorte);

        createUser("sofia.costa", "sofia.costa@atlanticologistica.pt", "Sofia Costa",
                ADMIN_PASSWORD, "ADMIN", atlanticoLogistica, null);
        createUser("pedro.matos", "pedro.matos@atlanticologistica.pt", "Pedro Matos",
                MANAGER_PASSWORD, "MANAGER", atlanticoLogistica, operacoesAtlantico);
        createUser("carla.nunes", "carla.nunes@atlanticologistica.pt", "Carla Nunes",
                EMPLOYEE_PASSWORD, "EMPLOYEE", atlanticoLogistica, armazemAtlantico);

        holidayRepository.save(demoHoliday(tejoDigital, LocalDate.of(2026, 6, 13), "Santo António"));
        holidayRepository.save(demoHoliday(norteSoftware, LocalDate.of(2026, 6, 24), "São João"));
        holidayRepository
                .save(demoHoliday(atlanticoLogistica, LocalDate.of(2026, 5, 20), "Dia da Região Autónoma dos Açores"));

        seedTimeBankSample(tejoDigital, inesMoura, tiagoFigueiredo);
    }

    private Holiday demoHoliday(Organization organization, LocalDate date, String name) {
        Holiday holiday = new Holiday();
        holiday.setOrganizationId(organization.getId());
        holiday.setDate(date);
        holiday.setName(name);
        return holiday;
    }

    // dados para o GET /api/time-bank/me da ines.moura dar um saldo com dias de vários tipos
    private void seedTimeBankSample(Organization organization, User user, User approver) {
        createApprovedTimeEntry(user, organization, LocalDate.of(2026, 7, 7), LocalTime.of(9, 0), LocalTime.of(18, 0),
                60, approver);
        createApprovedTimeEntry(user, organization, LocalDate.of(2026, 7, 8), LocalTime.of(9, 0), LocalTime.of(18, 0),
                60, approver);
        createApprovedTimeEntry(user, organization, LocalDate.of(2026, 7, 9), LocalTime.of(9, 0), LocalTime.of(18, 0),
                60, approver);
        createApprovedTimeEntry(user, organization, LocalDate.of(2026, 7, 10), LocalTime.of(9, 0), LocalTime.of(18, 0),
                60, approver);
        createApprovedTimeEntry(user, organization, LocalDate.of(2026, 7, 13), LocalTime.of(9, 0), LocalTime.of(19, 30),
                60, approver);

        AbsenceRequest vacation = new AbsenceRequest();
        vacation.setUser(user);
        vacation.setOrganization(organization);
        vacation.setStartDate(LocalDate.of(2026, 7, 14));
        vacation.setEndDate(LocalDate.of(2026, 7, 15));
        vacation.setType(AbsenceType.VACATION);
        vacation.setStatus(AbsenceStatus.APPROVED);
        vacation.setApprover(approver);
        vacation.setApprovedAt(Instant.now());
        absenceRequestRepository.save(vacation);
    }

    private void createApprovedTimeEntry(User user, Organization organization, LocalDate date, LocalTime start,
            LocalTime end, int breakMinutes, User approver) {
        TimeEntry entry = new TimeEntry();
        entry.setUser(user);
        entry.setOrganization(organization);
        entry.setWorkDate(date);
        entry.setStartTime(start);
        entry.setEndTime(end);
        entry.setBreakMinutes(breakMinutes);
        entry.setType(TimeEntryType.OFFICE);
        entry.setStatus(TimeEntryStatus.APPROVED);
        entry.setApprover(approver);
        entry.setApprovedAt(Instant.now());
        timeEntryRepository.save(entry);
    }

    private Organization createOrganization(String name, String timezone) {
        Organization organization = new Organization();
        organization.setName(name);
        organization.setTimezone(timezone);
        return organizationRepository.save(organization);
    }

    private Team createTeam(Organization organization, String name) {
        Team team = new Team();
        team.setName(name);
        team.setOrganization(organization);
        return teamRepository.save(team);
    }

    private User createUser(String username, String email, String fullName, String password, String roleName,
            Organization organization, Team team) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Missing demo role: " + roleName));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setOrganization(organization);
        user.setTeam(team);
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }
}
