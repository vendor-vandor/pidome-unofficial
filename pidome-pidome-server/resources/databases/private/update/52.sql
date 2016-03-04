CREATE TABLE [dashboards] (
  [id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
  [name] TEXT NOT NULL, 
  [clienttype] TEXT NOT NULL, 
  [clientid] INTEGER NOT NULL DEFAULT 0, 
  [personid] INTEGER NOT NULL DEFAULT 0, 
  [construct] TEXT NOT NULL);
INSERT INTO `dashboards` (`name`, `clienttype`, `personid`, `construct`) VALUES ('Default dashboard', 'WEB', 1, (SELECT `varcontent` FROM `arbitrarydata` WHERE `varname`='DashBoard.grid' LIMIT 1));
DELETE FROM `arbitrarydata` WHERE `varname`='DashBoard.grid';
PRAGMA `user_version`=52;