create table absence_requests (
    id uuid primary key,
    user_id uuid not null references users (id),
    organization_id uuid not null references organizations (id),
    start_date date not null,
    end_date date not null,
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

create index idx_absence_requests_user_dates on absence_requests (user_id, start_date, end_date);
