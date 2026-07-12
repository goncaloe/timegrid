create table log_events (
    id uuid primary key,
    organization_id uuid references organizations (id),
    user_id uuid references users (id),
    action varchar(40) not null,
    target_type varchar(30),
    target_id uuid,
    details varchar(500),
    ip_address varchar(45),
    user_agent varchar(255),
    created_at timestamp not null default now()
);

create index idx_log_events_org_created on log_events (organization_id, created_at);