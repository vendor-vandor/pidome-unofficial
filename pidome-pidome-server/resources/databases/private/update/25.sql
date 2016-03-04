ALTER TABLE installed_devices ADD COLUMN 'origin' BOOLEAN NOT NULL DEFAULT 0;
UPDATE installed_devices SET origin=1 WHERE name='blinkmsmartled';
PRAGMA user_version=25;