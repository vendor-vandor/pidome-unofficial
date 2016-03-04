ALTER TABLE clients_linked ADD COLUMN 'throttled' BOOLEAN NOT NULL DEFAULT 1;
PRAGMA user_version=29;