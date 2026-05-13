CREATE TABLE profile_addresses (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    country VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    postcode VARCHAR(50),
    address TEXT,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_profile_addresses_profile_id ON profile_addresses(profile_id);