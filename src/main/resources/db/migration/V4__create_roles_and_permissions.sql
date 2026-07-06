create table roles (
    id uuid primary key,
    name varchar(50) not null unique,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table permissions (
    id uuid primary key,
    name varchar(100) not null unique,
    description varchar(255),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table role_permissions (
    role_id uuid not null references roles (id),
    permission_id uuid not null references permissions (id),
    primary key (role_id, permission_id)
);

create table user_roles (
    user_id uuid not null references users (id),
    role_id uuid not null references roles (id),
    primary key (user_id, role_id)
);

insert into roles (id, name) values
    (gen_random_uuid(), 'ADMIN'),
    (gen_random_uuid(), 'MANAGER'),
    (gen_random_uuid(), 'EMPLOYEE');

insert into permissions (id, name, description) values
    (gen_random_uuid(), 'user:read', 'view users'),
    (gen_random_uuid(), 'user:write', 'create and edit users'),
    (gen_random_uuid(), 'team:write', 'create and edit teams'),
    (gen_random_uuid(), 'organization:write', 'edit organizations'),
    (gen_random_uuid(), 'time_entry:approve', 'approve time entries');

insert into role_permissions (role_id, permission_id)
select r.id, p.id 
from roles r, permissions p
where r.name = 'ADMIN';

insert into role_permissions (role_id, permission_id)
select r.id, p.id 
from roles r, permissions p
where r.name = 'MANAGER'
  and p.name in ('user:read', 'team:write', 'time_entry:approve');
