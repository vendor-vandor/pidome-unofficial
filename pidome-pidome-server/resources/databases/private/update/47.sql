ALTER TABLE tokensets ADD COLUMN 'master' BOOLEAN NOT NULL DEFAULT 0;
PRAGMA `user_version`=47;