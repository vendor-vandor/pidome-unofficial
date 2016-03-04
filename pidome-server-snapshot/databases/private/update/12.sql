INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('ACDetectionRFXCom','RFXCom AC: Detection','org.pidome.driver.device.rfxcom.rFXComLighting2Device','<device>
    <name>AC: Detection</name>
    <description>This device is used for detection with devices sending information using the AC protocl. This can be doorbells, movement detection, magnet contacts, etc..</description>
    <commandset>        
		<group id="deviceactions" label="Detection">
			<data description="Movement" datatype="boolean" id="switch" prefix="" suffix="" shortcut="1" visual="move" />
		</group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote''s address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('ACWallSwitchRFXCom','RFXCom AC: Wall Socket Switch','org.pidome.driver.device.rfxcom.rFXComLighting2Device','<device>
    <name>AC: Wall Switch</name>
    <description>This device is used to switch for example wall socket devices with the use of the AC protocol.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="hex" hidden="false" shortcut="1" id="switch">
                <on value="1" label="On"/>
                <off value="0" label="Off"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote''s address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('ACWallSwitchDIMRFXCom','RFXCom AC: Dimmable Wall Socket Switch','org.pidome.driver.device.rfxcom.rFXComLighting2Device','<device>
    <name>AC: Dimmable Wall Socket Switch</name>
    <description>This device is used with wall socket switches which are capable of dimming using the AC protocol</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="hex" hidden="false" shortcut="1" id="switch">
                <on value="1" label="On"/>
                <off value="0" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="dimlevel" shortcut="2" description="Set the light''s dim level" />
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote''s address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('ACGroupedWallSwitchesRFXCom','RFXCom AC: Grouped Wall Socket Switches','org.pidome.driver.device.rfxcom.rFXComLighting2Device','<device>
    <name>AC: Grouped Wall Switches</name>
    <description>This device is used to switch a group of for example wall socket devices with the use of the AC protocol. Allthough not a real device, it can be used as such in the server</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="hex" hidden="false" shortcut="1" id="groupswitch">
                <on value="1" label="On"/>
                <off value="0" label="Off"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote''s address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('ACGroupedWallSwitchesDIMRFXCom','RFXCom AC: Grouped Dimmable Wall Socket Switches','org.pidome.driver.device.rfxcom.rFXComLighting2Device','<device>
    <name>AC: Dimmable Wall Socket Switch</name>
    <description>This device is used with wall socket switches which are capable of dimming using the AC protocol</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="hex" hidden="false" shortcut="1" id="groupswitch">
                <on value="1" label="On"/>
                <off value="0" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="groupdimlevel" shortcut="2" description="Set the light''s dim level" />
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote''s address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
PRAGMA user_version=12;