CREATE TABLE [customevents] (
  [id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
  [identifier] TEXT NOT NULL, 
  [name] TEXT NOT NULL, 
  [description] TEXT NOT NULL, 
  [last_occurrence] DATETIME, 
  [last_occurrence_remark] TEXT, 
  [created] DATETIME NOT NULL DEFAULT (datetime('now')), 
  [modified] DATETIME NOT NULL DEFAULT (datetime('now')));
PRAGMA user_version=27;