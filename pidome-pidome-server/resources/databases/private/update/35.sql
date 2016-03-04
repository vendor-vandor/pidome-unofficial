DROP TABLE scenes;
CREATE TABLE [scenes] (
  [id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
  [name] VARCHAR NOT NULL, 
  [description] TEXT NOT NULL, 
  [dependencies] TEXT NOT NULL, 
  [created] DATETIME NOT NULL DEFAULT (datetime('now')), 
  [modified] DATETIME DEFAULT (datetime('now')));
PRAGMA user_version=35;