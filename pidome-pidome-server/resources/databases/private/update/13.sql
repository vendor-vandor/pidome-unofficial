UPDATE installed_drivers SET hascustom=1,customdevicepath='org.pidome.driver.device.rfxcom.rFXComCustomDevice',peripheral_driver=(SELECT p.id FROM installed_peripherals p WHERE p.name='FTDIFT232RL' LIMIT 1) WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER';
PRAGMA user_version=13;