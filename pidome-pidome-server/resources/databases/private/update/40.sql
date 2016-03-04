CREATE TABLE [notificationlog] (
  [id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
  [datetime] DATETIME NOT NULL DEFAULT (datetime('now')), 
  [originates] VARCHAR NOT NULL, 
  [type] VARCHAR NOT NULL, 
  [subject] varchar NOT NULL, 
  [message] VARCHAR, 
  [read] BOOLEAN NOT NULL DEFAULT 0);
PRAGMA user_version=40;