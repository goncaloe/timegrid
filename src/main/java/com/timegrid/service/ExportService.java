package com.timegrid.service;

import com.timegrid.common.Messages;
import com.timegrid.common.WorkingDays;
import com.timegrid.dto.ExportFile;
import com.timegrid.entity.AbsenceRequest;
import com.timegrid.entity.TimeEntry;
import com.timegrid.enums.LogAction;
import com.timegrid.enums.ExportFormat;
import com.timegrid.exception.BusinessRuleException;
import com.timegrid.repository.AbsenceRequestRepository;
import com.timegrid.repository.TimeEntryRepository;
import com.timegrid.security.CurrentUser;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportService {

    private final TimeEntryRepository timeEntryRepository;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final CurrentUser currentUser;
    private final Messages messages;
    private final LogService logService;
    private final Map<ExportFormat, ReportWriter> writers;

    public ExportService(TimeEntryRepository timeEntryRepository, AbsenceRequestRepository absenceRequestRepository,
            CurrentUser currentUser, Messages messages, LogService logService, List<ReportWriter> writers) {
        this.timeEntryRepository = timeEntryRepository;
        this.absenceRequestRepository = absenceRequestRepository;
        this.currentUser = currentUser;
        this.messages = messages;
        this.logService = logService;
        this.writers = writers.stream().collect(Collectors.toMap(ReportWriter::format, Function.identity()));
    }

    @Transactional(readOnly = true)
    public ExportFile timeEntries(ExportFormat format, LocalDate from, LocalDate to, UUID userId, UUID teamId) {
        checkPeriod(from, to);
        Locale locale = LocaleContextHolder.getLocale();
        List<TimeEntry> entries = timeEntryRepository.findForExport(currentUser.organizationId(), from, to, userId,
                teamId);

        List<String> headers = List.of(
                messages.get("export.header.user", locale),
                messages.get("export.header.date", locale),
                messages.get("export.header.start", locale),
                messages.get("export.header.end", locale),
                messages.get("export.header.break", locale),
                messages.get("export.header.duration", locale),
                messages.get("export.header.type", locale),
                messages.get("export.header.status", locale),
                messages.get("export.header.notes", locale));
        List<List<Object>> rows = new ArrayList<>();
        for (TimeEntry entry : entries) {
            long minutes = Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes()
                    - entry.getBreakMinutes();
            rows.add(Arrays.asList(
                    entry.getUser().getFullName(),
                    entry.getWorkDate(),
                    entry.getStartTime(),
                    entry.getEndTime(),
                    entry.getBreakMinutes(),
                    minutes,
                    messages.get("export.time-entry-type." + entry.getType().name(), locale),
                    messages.get("export.status." + entry.getStatus().name(), locale),
                    entry.getNotes()));
        }
        ReportTable table = new ReportTable(messages.get("export.time-entries.title", locale), headers, rows);
        return generate(format, table, "entries", "time-entries", from, to);
    }

    @Transactional(readOnly = true)
    public ExportFile absences(ExportFormat format, LocalDate from, LocalDate to, UUID userId, UUID teamId) {
        checkPeriod(from, to);
        Locale locale = LocaleContextHolder.getLocale();
        List<AbsenceRequest> requests = absenceRequestRepository.findForExport(currentUser.organizationId(), from, to,
                userId, teamId);

        List<String> headers = List.of(
                messages.get("export.header.user", locale),
                messages.get("export.header.start", locale),
                messages.get("export.header.end", locale),
                messages.get("export.header.working-days", locale),
                messages.get("export.header.type", locale),
                messages.get("export.header.status", locale),
                messages.get("export.header.notes", locale));
        List<List<Object>> rows = new ArrayList<>();
        for (AbsenceRequest request : requests) {
            rows.add(Arrays.asList(
                    request.getUser().getFullName(),
                    request.getStartDate(),
                    request.getEndDate(),
                    WorkingDays.between(request.getStartDate(), request.getEndDate()),
                    messages.get("export.absence-type." + request.getType().name(), locale),
                    messages.get("export.status." + request.getStatus().name(), locale),
                    request.getNotes()));
        }
        ReportTable table = new ReportTable(messages.get("export.absences.title", locale), headers, rows);
        return generate(format, table, "absences", "absences", from, to);
    }

    private ExportFile generate(ExportFormat format, ReportTable table, String name, String kind, LocalDate from,
            LocalDate to) {
        ReportWriter writer = writers.get(format);
        if (writer == null) {
            throw new BusinessRuleException("export.invalid-format", format.extension());
        }
        byte[] content = writer.write(table);
        logService.record(currentUser.organizationId(), currentUser.userId(), LogAction.EXPORT_GENERATED,
                "EXPORT", null,
                kind + " " + format.extension() + " " + from + ".." + to + " (" + table.rows().size() + " lines)");
        return new ExportFile(filename(name, from, to, format), format, content);
    }

    private static String filename(String name, LocalDate from, LocalDate to, ExportFormat format) {
        YearMonth month = YearMonth.from(from);
        boolean wholeMonth = from.equals(month.atDay(1)) && to.equals(month.atEndOfMonth());
        String period = wholeMonth ? month.toString() : from + "_" + to;
        return "timegrid-" + name + "-" + period + "." + format.extension();
    }

    private static void checkPeriod(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessRuleException("export.invalid-period");
        }
    }

}