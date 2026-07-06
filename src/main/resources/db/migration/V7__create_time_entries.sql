create table time_entries (
    id uuid primary key,
    user_id uuid not null references users (id),
    organization_id uuid not null references organizations (id),
    work_date date not null,
    start_time time not null,
    end_time time not null,
    break_minutes integer not null default 0,
    type varchar(20) not null,
    notes varchar(500),
    status varchar(20) not null,
    rejection_reason varchar(500),
    approver_id uuid references users (id),
    approved_at timestamp,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    deleted_at timestamp
);

create index idx_time_entries_user_work_date on time_entries (user_id, work_date);
