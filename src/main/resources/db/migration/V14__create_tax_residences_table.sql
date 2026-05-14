CREATE TABLE tax_residences (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    country VARCHAR(255) NOT NULL,
    inn VARCHAR(255)
);

CREATE INDEX idx_tax_residences_profile_id ON tax_residences(profile_id);