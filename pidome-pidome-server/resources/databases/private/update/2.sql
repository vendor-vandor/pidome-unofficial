ALTER TABLE installed_devices ADD COLUMN struct TEXT NOT NULL DEFAULT '{"type":0}';
PRAGMA user_version=2;