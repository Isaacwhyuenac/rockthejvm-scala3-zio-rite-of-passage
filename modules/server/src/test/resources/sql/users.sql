CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    hashed_password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS recovery_tokens (
   email       TEXT    PRIMARY KEY,
   token       TEXT    NOT NULL,
   expiration  BIGINT  NOT NULL
);