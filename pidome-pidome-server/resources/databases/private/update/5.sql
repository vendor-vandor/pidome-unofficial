ALTER TABLE clients ADD COLUMN cpwd BOOLEAN NOT NULL DEFAULT 1;
ALTER TABLE clients ADD COLUMN fixed BOOLEAN NOT NULL DEFAULT 1;
ALTER TABLE clients ADD COLUMN roleset TEXT;
ALTER TABLE clients ADD COLUMN ext BOOLEAN NOT NULL DEFAULT 0;
UPDATE clients SET clienttype='ADMIN';
CREATE TABLE [clients_linked] ([id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, [devicelogin] TEXT, [deviceinfo] TEXT, [created] DATETIME NOT NULL DEFAULT (datetime('now')), [binding] INTEGER NOT NULL CONSTRAINT [client_bind] REFERENCES [clients]([id]) ON DELETE CASCADE ON UPDATE CASCADE);
PRAGMA user_version=5;