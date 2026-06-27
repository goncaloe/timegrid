create table users (
    id uuid primary key,
    username varchar(100) not null unique,
    email varchar(150) not null unique,
    full_name varchar(150) not null,
    password_hash varchar(255) not null,
    status varchar(20) not null,
    organization_id uuid not null references organizations (id),
    team_id uuid references teams (id),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    deleted_at timestamp
);
