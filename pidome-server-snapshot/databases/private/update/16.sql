INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeLightwaveRF3000WRelay','RFXCom: Lightwave 3000W relay','org.pidome.driver.device.rfxcom.rFXComLighting5Device','<device>
    <name>LightwaveRF 3000Watt relay switch</name>
    <description>With this device you can control a 3000W relay switch.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch open/close" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="Open"/>
                <off value="false" label="Close"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="LIGHTING5" label="Lightning 5" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="LIGHTWAVERF" label="LightwaveRF" />
        </select>
    </options>
    <address>
        <description>This is the address of the wall socket. Structure is 00,00,00:0 (hex,hex,hex:number). The hex values are the device id and the number is the unit code</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_RFXCOMSUPPORT_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-RFXCOM-SUPPORT' LIMIT 1));
PRAGMA user_version=16;
