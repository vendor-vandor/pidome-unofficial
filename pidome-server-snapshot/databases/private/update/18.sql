UPDATE installed_drivers SET customdevicepath='org.pidome.driver.device.pidomeNativeMySensorsMQTTDevice14' WHERE driverid='NATIVE_MQTTMYSENSORS_DRIVER';
UPDATE installed_devices SET driver='org.pidome.driver.device.pidomeNativeMySensorsMQTTDevice14' WHERE driver='org.pidome.plugins.devices.mySensorsMQTTDevices14';
PRAGMA user_version=18;