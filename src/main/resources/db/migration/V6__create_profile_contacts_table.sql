CREATE TABLE profile_contacts (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profile(id),
    contact_type VARCHAR(20) NOT NULL CHECK (contact_type IN ('email', 'phone')),
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    value VARCHAR(255) NOT NULL
);

CREATE INDEX idx_profile_contacts_profile_id ON profile_contacts(profile_id);