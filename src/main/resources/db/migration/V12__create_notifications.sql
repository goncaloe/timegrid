create table notifications (
    id uuid primary key,
    recipient_id uuid not null references users (id),
    organization_id uuid not null references organizations (id),
    category varchar(20) not null,
    title varchar(200) not null,
    message varchar(1000) not null,
    link varchar(255),
    read_at timestamp,
    created_at timestamp not null default now()
);

create index idx_notifications_recipient_created on notifications (recipient_id, created_at);