ALTER TABLE installed_devices ADD COLUMN 'sequence' INTEGER NOT NULL DEFAULT 1;
ALTER TABLE installed_packages ADD COLUMN 'sequence' INTEGER NOT NULL DEFAULT 1;
PRAGMA user_version=21;