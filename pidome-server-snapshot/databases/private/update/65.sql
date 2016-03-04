ALTER TABLE devices ADD COLUMN 'modifiers' TEXT NOT NULL DEFAULT "[]";
INSERT INTO installed_plugins (`name`,`type`,`path`,`package`,`active`,`fixed`) VALUES ('Evening light script',7,'org.pidome.plugins.modifiers.timeBasedLighting',(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-MODIFIERS' LIMIT 1),1,0);
PRAGMA user_version=65;