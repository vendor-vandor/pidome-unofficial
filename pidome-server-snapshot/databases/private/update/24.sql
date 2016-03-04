INSERT INTO installed_plugins ('name','type','path','package','active','fixed') VALUES ('PiDome Buienradar',6,'org.pidome.plugins.weatherplugins.pidomeBuienRadar',(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-WEATHERPLUGINS' LIMIT 1),1,1);
PRAGMA user_version=24;
