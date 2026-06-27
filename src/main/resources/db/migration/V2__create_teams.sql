create table teams (
    id uuid primary key,
    name varchar(150) not null,
    description varchar(500),
    organization_id uuid not null references organizations (id),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    unique (organization_id, name)
);
