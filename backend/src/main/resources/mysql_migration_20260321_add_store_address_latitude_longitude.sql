-- Manual MySQL migration: add store location fields to da_store.
-- Run once on the target database.

ALTER TABLE da_store
    ADD COLUMN address VARCHAR(255) NULL COMMENT 'store address' AFTER country_code,
    ADD COLUMN latitude DECIMAL(10, 7) NULL COMMENT 'store latitude' AFTER address,
    ADD COLUMN longitude DECIMAL(10, 7) NULL COMMENT 'store longitude' AFTER latitude;

-- Optional: align initial demo rows with data.sql seed values.
UPDATE da_store
SET address = 'No.18 Chinda Road, Shanghai',
    latitude = 31.2304160,
    longitude = 121.4737010
WHERE id = 2;

UPDATE da_store
SET address = '188 Rama IV Rd, Bangkok',
    latitude = 13.7304220,
    longitude = 100.5231860
WHERE id = 54;
