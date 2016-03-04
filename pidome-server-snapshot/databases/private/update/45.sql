INSERT INTO `installed_peripherals` (`name`,`friendlyname`,`driver`,`vid`,`pid`,`version`,`type`,`interface_type`,`selectable`,`package`) VALUES ('PiDomeNativeUSBSerial','Pidome USB Serial implementation','org.pidome.driver.peripherals.pidomeNativeUSBSerial','PiDome','NativeSerial','0.0.1','TYPE_USB','SERIAL',1,1);
UPDATE `installed_peripherals` SET `selectable`=0;
UPDATE `installed_peripherals` SET `selectable`=1 WHERE `vid`='PiDome' AND `pid`='NativeSerial';
UPDATE `installed_peripherals` SET `driver`='org.pidome.driver.peripherals.pidomeNativeUSBSerial' WHERE `vid`='2341';
PRAGMA user_version=45;