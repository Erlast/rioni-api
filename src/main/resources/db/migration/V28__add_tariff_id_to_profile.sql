ALTER TABLE profile ADD COLUMN IF NOT EXISTS tariff_id BIGINT REFERENCES tariffs(id);

CREATE INDEX IF NOT EXISTS idx_profile_tariff_id ON profile(tariff_id);
