UPDATE installed_drivers SET package=(SELECT p.id FROM installed_packages p WHERE p.packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1) WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER';
PRAGMA user_version=15;