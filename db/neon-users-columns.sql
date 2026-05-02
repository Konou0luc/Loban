ALTER TABLE users
    ADD COLUMN IF NOT EXISTS transporter_profile_complete BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profile_photo_data_url TEXT;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS transporter_security_info TEXT;

ALTER TABLE users ADD COLUMN IF NOT EXISTS vehicle_type VARCHAR(40);
ALTER TABLE users ADD COLUMN IF NOT EXISTS coverage_area VARCHAR(160);
ALTER TABLE users ADD COLUMN IF NOT EXISTS years_experience INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS delivery_categories TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS availability VARCHAR(40);
ALTER TABLE users ADD COLUMN IF NOT EXISTS driving_license_number VARCHAR(120);
ALTER TABLE users ADD COLUMN IF NOT EXISTS identity_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS license_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS transporter_confirmed_badge BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS transporter_availability_slots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_slot_range CHECK (end_at > start_at)
);

CREATE INDEX IF NOT EXISTS idx_av_slots_user_start ON transporter_availability_slots (user_id, start_at);

ALTER TABLE transport_requests ADD COLUMN IF NOT EXISTS desired_slot_start TIMESTAMPTZ;
ALTER TABLE transport_requests ADD COLUMN IF NOT EXISTS desired_slot_end TIMESTAMPTZ;
