create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null references users (id),
    token_hash varchar(64) not null unique,
    expires_at timestamp not null,
    revoked_at timestamp,
    replaced_by uuid references refresh_tokens (id),
    created_at timestamp not null default now()
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
