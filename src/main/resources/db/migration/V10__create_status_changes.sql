create table status_changes (
    id uuid primary key,
    entity_type varchar(20) not null,
    entity_id uuid not null,
    from_status varchar(20),
    to_status varchar(20) not null,
    changed_by uuid not null references users (id),
    comment varchar(500),
    changed_at timestamp not null default now()
);

create index idx_status_changes_entity on status_changes (entity_type, entity_id, changed_at);