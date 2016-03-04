INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComTempSensor','RFXCom: Temperature sensor','org.pidome.driver.device.rfxcom.rFXComOregon50Device','<device>
    <name>Temperature sensor</name>
    <description>Sensor supplying temperature</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" graph="time-series" visual="temperature"/>
        </group>
        <group id="device" label="Device status">
            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%"/>
            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix=""/>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="OREGON" label="Oregon protocol" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="TYPE1" label="THR128/138, THC138" />
			<option value="TYPE2" label="THC238/268,THN132,THWR288,THRN122,THN122,AW129/131" />
			<option value="TYPE3" label="THWR800" />
			<option value="TYPE4" label="RTHN318" />
			<option value="TYPE5" label="La Crosse TX3, TX4, TX17" />
			<option value="TYPE6" label="TS15C" />
			<option value="TYPE7" label="Viking 02811" />
			<option value="TYPE8" label="La Crosse WS2300La" />
			<option value="TYPE9" label="RUBiCSON" />
			<option value="TYPE10" label="TFA 30.3133" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComHumidSensor','RFXCom: Humidity sensor','org.pidome.driver.device.rfxcom.rFXComOregon51Device','<device>
    <name>Humidity sensor</name>
    <description>Sensor Humidity data</description>
    <commandset>
        <group id="environment" label="Environment">
			<data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" shortcut="1" graph="time-series"/>
			<data description="Humidity status" datatype="string" id="humiditystat" value="humiditystat" prefix="" suffix=""/>
        </group>
        <group id="device" label="Device status">
            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%"/>
            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix=""/>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="OREGON" label="Oregon protocol" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="TYPE1" label="LaCrosse TX3" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComTempHumSensor','RFXCom: Temperature and humidity sensor','org.pidome.driver.device.rfxcom.rFXComOregon52Device','<device>
    <name>Temperature and humidity sensor</name>
    <description>Sensor supplying temperature and humidity</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" graph="time-series" visual="temperature"/>
			<data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" shortcut="2" graph="time-series"/>
			<data description="Humidity status" datatype="string" id="humiditystat" value="humiditystat" prefix="" suffix=""/>
        </group>
        <group id="device" label="Device status">
            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%"/>
            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix=""/>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="OREGON" label="Oregon protocol" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="TYPE1" label="THGN122/123, THGN132, THGR122/228/238/268" />
			<option value="TYPE2" label="THGR810, THGN800" />
			<option value="TYPE3" label="RTGR328" />
			<option value="TYPE4" label="THGR328" />
			<option value="TYPE5" label="WTGR800" />
			<option value="TYPE6" label="THGR918, THGRN228, THGN50" />
			<option value="TYPE7" label="TFA TS34C, Cresta" />
			<option value="TYPE8" label="WT260,WT260H,WT440H,WT450,WT450H" />
			<option value="TYPE9" label="Viking 02035, 02038" />
			<option value="TYPE10" label="Rubicson" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComTempSensor','RFXCom: Temperature, humidity and barometric sensor','org.pidome.driver.device.rfxcom.rFXComOregon54Device','<device>
    <name>Temperature, humidity and barometric sensor</name>
    <description>Sensor supplying temperature, humidity and barometric data</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" graph="time-series" visual="temperature"/>
			<data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" shortcut="2" graph="time-series"/>
			<data description="Humidity status" datatype="string" id="humiditystat" value="humiditystat" prefix="" suffix=""/>
			<data description="Pressure" datatype="float" id="pressure" value="pressure" prefix="" suffix="" shortcut="3" graph="time-series"/>
			<data description="Forecast" datatype="string" id="forecast" value="forecast" prefix="" suffix=""/>
        </group>
        <group id="device" label="Device status">
            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%"/>
            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix=""/>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="OREGON" label="Oregon protocol" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="TYPE1" label="BTHR918" />
			<option value="TYPE2" label="BTHR918N, BTHR968" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
PRAGMA user_version=17;