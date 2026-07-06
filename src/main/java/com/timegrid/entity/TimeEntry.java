package com.timegrid.entity;

import com.timegrid.enums.TimeEntryStatus;
import com.timegrid.enums.TimeEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
@NoArgsConstructor
public class TimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_minutes", nullable = false)
    private int breakMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeEntryType type;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeEntryStatus status = TimeEntryStatus.DRAFT;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "approved_at")
    private Instant approvedAt;

    private Instant deletedAt;
}
