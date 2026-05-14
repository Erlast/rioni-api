CREATE TABLE residence_permits (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    country VARCHAR(255) NOT NULL,
    issued_by TEXT,
    document_number VARCHAR(255),
    stay_period VARCHAR(255)
);

CREATE INDEX idx_residence_permits_profile_id ON residence_permits(profile_id);