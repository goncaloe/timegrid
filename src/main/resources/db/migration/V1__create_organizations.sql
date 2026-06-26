create table organizations (
    id uuid primary key,
    name varchar(150) not null unique,
    status varchar(20) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    deleted_at timestamp
);
