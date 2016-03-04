INSERT INTO plugin_types ('name', 'description') VALUES ('Weather plugins', 'Plugins used to provide weather data');
INSERT INTO installed_packages ('packageid','name','version','active', 'install_base','package_type','author', 'email','website') VALUES ('PIDOME-NATIVE-WEATHERPLUGINS', 'PiDome.WeatherPlugins', '0.0.1', 1, 'PiDome.WeatherPlugins.jar','plugin', 'PiDome','support@pidome.org', 'http://pidome.org');
INSERT INTO installed_plugins ('name','type','path','package','active','fixed') VALUES ('PiDome OpenWeatherMap',6,'org.pidome.plugins.weatherplugins.pidomeOpenWeatherMap',(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-WEATHERPLUGINS' LIMIT 1),1,1);
PRAGMA user_version=23;
