CREATE TABLE [scripted_drivers] (
  [id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
  [name] TEXT NOT NULL, 
  [description] TEXT NOT NULL, 
  [scriptcontent] TEXT NOT NULL, 
  [driverid] INTEGER NOT NULL CONSTRAINT [driver_link] REFERENCES [installed_drivers]([id]) ON DELETE CASCADE ON UPDATE CASCADE, 
  [fixed] BOOLEAN NOT NULL DEFAULT 0, 
  [editable] BOOLEAN NOT NULL DEFAULT 1, 
  [created] DATETIME NOT NULL DEFAULT (datetime('now')), 
  [modified] DATETIME NOT NULL DEFAULT (datetime('now'))) ;
UPDATE installed_drivers SET `driverid`='NATIVE_PIDOMEBAREBONESERIAL_DRIVER',`name`='Serial@Barebone.Pidome',`friendlyname`='Barebone custom driver for serial devices',`driver`='org.pidome.driver.driver.nativeCustomSerialDriver',`hascustom`=1,`customdevicepath`='org.pidome.driver.driver.nativeCustomSerialDriver.nativeCustomSerialDevice' WHERE `driverid`='NATIVE_PIDOMEARDUINOBAREBONE_DRIVER';
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('customserialdevice','Custom serial device','org.pidome.driver.driver.nativeCustomSerialDriver.nativeCustomSerialDevice','{"device":{"address":{"input":{"datatype":"string","description":"Custom address","type":"text"},"description":"A Custom address"},"options":[],"name":"Custom serial device","description":"Thsi is a custom serial device for testing","controlset":{"groups":[{"controls":[{"shortcut":0,"hidden":false,"datatype":"boolean","extra":"","description":"Switch a led","id":"switchled","type":"toggle","parameters":{"off":{"label":"Off","value":"false"},"on":{"label":"On","value":"true"}},"retention":false}],"id":"group","label":"A group"}]}}}',1,1,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_PIDOMEBAREBONESERIAL_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-DRIVERS-PACKAGE' LIMIT 1));
PRAGMA user_version=59;