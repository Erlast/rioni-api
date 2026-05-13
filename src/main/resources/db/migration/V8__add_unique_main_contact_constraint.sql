CREATE UNIQUE INDEX idx_profile_contacts_main_contact
ON profile_contacts(profile_id, contact_type)
WHERE is_main = true;