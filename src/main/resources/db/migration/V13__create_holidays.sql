create table holidays (
    id uuid primary key,
    organization_id uuid not null references organizations (id),
    date date not null,
    name varchar(150) not null,
    created_at timestamp not null default now()
);

create index idx_holidays_organization_date on holidays (organization_id, date);