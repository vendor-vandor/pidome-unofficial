ALTER TABLE installed_plugins ADD COLUMN 'version' TEXT NOT NULL DEFAULT "0.0.1";
ALTER TABLE installed_plugins ADD COLUMN 'sequence' INTEGER NOT NULL DEFAULT 0;
PRAGMA user_version=42;