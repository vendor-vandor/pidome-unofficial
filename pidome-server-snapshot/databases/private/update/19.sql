INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComLighting1SwitchDevice','RFXCom: Lighting1 switch device','org.pidome.driver.device.rfxcom.rFXComLighting1Device','<device>
    <name>Lighting 1 switch</name>
    <description>With this device you can switch on/off Lighting 1 devices</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch on/off" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="On"/>
                <off value="false" label="Of"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="LIGHTING1" label="Lightning 1" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="X10" label="X10" />
            <option value="ARC" label="ARC protocl" />
            <option value="ELRO_AB400D" label="ELRO AB400D" />
            <option value="WAVEMAN" label="Waveman" />
            <option value="EMW200" label="EMW 200" />
            <option value="IMPULS" label="Impuls" />
            <option value="RISING_SUN" label="RisingSun" />
            <option value="PHILIPS_SBC" label="Philips SBC" />
            <option value="ENERGENIE" label="Energenie" />
            <option value="ENERGENIE5" label="Energenie 5" />
            <option value="COCO_GDR2" label="COCO GDR2" />
        </select>
    </options>
    <address>
        <description>This is the address of this device. Structure is A-P:1-64 (letter:number).Please refer to the RFXCom documentation on witch letter and number to use depending on the switches on the device.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComLighting1DimmableSwitchDevice','RFXCom: Lighting1 dimmable switch device','org.pidome.driver.device.rfxcom.rFXComLighting1Device','<device>
    <name>Lighting 1 dimmable switch</name>
    <description>With this device you can dim/brighten and switch on/off Lighting 1 devices</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch on/off" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="On"/>
                <off value="false" label="Of"/>
            </toggle>
            <button id="dim" datatype="string" value="dim" description="Dim" label="Dim" shortcut="2" />
            <button id="bright" datatype="string" value="bright" description="Brighten/Bright" label="Bright" shortcut="3" />
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="LIGHTING1" label="Lightning 1" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="X10" label="X10" />
            <option value="ARC" label="ARC protocl" />
            <option value="ELRO_AB400D" label="ELRO AB400D" />
            <option value="WAVEMAN" label="Waveman" />
            <option value="EMW200" label="EMW 200" />
            <option value="IMPULS" label="Impuls" />
            <option value="RISING_SUN" label="RisingSun" />
            <option value="PHILIPS_SBC" label="Philips SBC" />
            <option value="ENERGENIE" label="Energenie" />
            <option value="ENERGENIE5" label="Energenie 5" />
            <option value="COCO_GDR2" label="COCO GDR2" />
        </select>
    </options>
    <address>
        <description>This is the address of this device. Structure is A-P:1-64 (letter:number).Please refer to the RFXCom documentation on witch letter and number to use depending on the switches on the device.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComLighting1GroupSwitchDevice','RFXCom: Lighting1 group switch device','org.pidome.driver.device.rfxcom.rFXComLighting1Device','<device>
    <name>Lighting 1 group switch</name>
    <description>With this device you can switch on/off Lighting 1 group of devices</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch group on/off" datatype="boolean" hidden="false" shortcut="1" id="groupswitch">
                <on value="true" label="On"/>
                <off value="false" label="Of"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="LIGHTING1" label="Lightning 1" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="X10" label="X10" />
            <option value="ARC" label="ARC protocl" />
            <option value="ELRO_AB400D" label="ELRO AB400D" />
            <option value="WAVEMAN" label="Waveman" />
            <option value="EMW200" label="EMW 200" />
            <option value="IMPULS" label="Impuls" />
            <option value="RISING_SUN" label="RisingSun" />
            <option value="PHILIPS_SBC" label="Philips SBC" />
            <option value="ENERGENIE" label="Energenie" />
            <option value="ENERGENIE5" label="Energenie 5" />
            <option value="COCO_GDR2" label="COCO GDR2" />
        </select>
    </options>
    <address>
        <description>This is the address of this device. Structure is A-P:1-64 (letter:number).Please refer to the RFXCom documentation on witch letter and number to use depending on the switches on the device.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComLighting1ChimeDevice','RFXCom: Lighting1 chime device','org.pidome.driver.device.rfxcom.rFXComLighting1Device','<device>
    <name>Lighting 1 Chime</name>
    <description>This device detects lighting 1 chimes</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <data description="Chime" datatype="boolean" id="chime" prefix="" suffix="" shortcut="1" />
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="LIGHTING1" label="Lightning 1" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="X10" label="X10" />
            <option value="ARC" label="ARC protocl" />
            <option value="ELRO_AB400D" label="ELRO AB400D" />
            <option value="WAVEMAN" label="Waveman" />
            <option value="EMW200" label="EMW 200" />
            <option value="IMPULS" label="Impuls" />
            <option value="RISING_SUN" label="RisingSun" />
            <option value="PHILIPS_SBC" label="Philips SBC" />
            <option value="ENERGENIE" label="Energenie" />
            <option value="ENERGENIE5" label="Energenie 5" />
            <option value="COCO_GDR2" label="COCO GDR2" />
        </select>
    </options>
    <address>
        <description>This is the address of this device. Structure is A-P:1-64 (letter:number).Please refer to the RFXCom documentation on witch letter and number to use depending on the switches on the device.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComRainSensor','RFXCom: Rain sensor (55)','org.pidome.driver.device.rfxcom.rFXComOregon55Device','<device>
    <name>Rain sensor</name>
    <description>Sensor supplying rain data</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Current" datatype="float" id="raincurrent" value="raincurrent" prefix="" suffix="" shortcut="1" graph="time-series" />
            <data description="Total" datatype="float" id="raintotal" value="raintotal" prefix="" suffix="" shortcut="2" graph="time-totals" />
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
            <option value="TYPE1" label="RGR126/682/918" />
            <option value="TYPE2" label="PCR800" />
            <option value="TYPE3" label="TFA" />
            <option value="TYPE4" label="UPM RG700" />
            <option value="TYPE5" label="WS2300" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeRFXComRainSensor','RFXCom: Wind sensor (56)','org.pidome.driver.device.rfxcom.rFXComOregon56Device','<device>
    <name>Wind sensor</name>
    <description>Sensor supplying temperature</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Wind speed" datatype="float" id="windspeed" value="windspeed" prefix="" suffix="" shortcut="1" graph="time-series" />
            <data description="Wind direction" datatype="float" id="winddirection" value="winddirection" prefix="" suffix="" shortcut="2" />
            <data description="Wind gust" datatype="float" id="windgust" value="windgust" prefix="" suffix="" shortcut="3" />
            <data description="Wind chill" datatype="float" id="windchill" value="windchill" prefix="" suffix="°C" />
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
            <option value="TYPE1" label="WTGR800" />
            <option value="TYPE2" label="WGR800" />
            <option value="TYPE3" label="STR918, WGR918" />
            <option value="TYPE4" label="TFA (WIND4)" />
            <option value="TYPE5" label="UPM WDS500" />
            <option value="TYPE6" label="WS2300" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
PRAGMA user_version=19;