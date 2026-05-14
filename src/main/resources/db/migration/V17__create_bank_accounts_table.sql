CREATE TABLE bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    country VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    iban VARCHAR(255),
    swift VARCHAR(255),
    is_main BOOLEAN DEFAULT FALSE,
    is_confirmed BOOLEAN DEFAULT FALSE,
    is_blocked BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_bank_accounts_profile_id ON bank_accounts(profile_id);