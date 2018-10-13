
create table users (
  id uuid primary key,
  email varchar not null,
  name varchar not null,
  password varchar not null,
  created_at timestamptz not null default now()
);

create unique index on users (lower(email) varchar_pattern_ops);

create table tokens (
  token varchar primary key,
  user_id uuid not null references users(id),
  expires_at timestamptz not null,
  created_at timestamptz not null default now(),
  deleted_at timestamptz
);
