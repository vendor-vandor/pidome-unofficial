INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeLightwaveRFSocketSwitch','RFXCom: Lightwave Socket switch','org.pidome.driver.device.rfxcom.rFXComLighting5Device','<device>
    <name>LightwaveRF Socket Locker</name>
    <description>With this device you can lock LightwaveRF sockets.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Socket lock" datatype="boolean" hidden="false" shortcut="1" id="lockswitch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="level" shortcut="2" description="Set level" />
			<button id="alllock" datatype="string" value="alllock" description="Lock All" label="Lock All" />
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
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeLightwaveRFNoGroupRemote','RFXCom: Lightwave Remote (No group)','org.pidome.driver.device.rfxcom.rFXComLighting5Device','<device>
    <name>LightwaveRF Remote (non group)</name>
    <description>With this device you can control like with a normal remote.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Swich on/off" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="level" shortcut="2" description="Set level" />
			<button id="groupoff" datatype="string" value="groupoff" description="All off" label="All Off" />
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
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeLightwaveRFGroupRemote','RFXCom: Lightwave Group remote','org.pidome.driver.device.rfxcom.rFXComLighting5Device','<device>
    <name>LightwaveRF Remote group actions</name>
    <description>With this device you can control like with a normal remote.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
			<button id="groupoff" datatype="string" value="groupoff" description="All off" label="All Off" shortcut="1"/>
			<button id="moodselect1" datatype="string" value="moodselect1" description="Mood 1" label="1"/>
			<button id="moodselect2" datatype="string" value="moodselect2" description="Mood 2" label="2"/>
			<button id="moodselect3" datatype="string" value="moodselect3" description="Mood 3" label="3" />
			<button id="moodselect4" datatype="string" value="moodselect4" description="Mood 4" label="4" />
			<button id="moodselect5" datatype="string" value="moodselect5" description="Mood 5" label="5" />
			<slider min="0" max="100" id="level" description="Set level" shortcut="2"/>
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
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeLightwaveRFRelaySwitch500W','RFXCom: Lightwave 500W relay switch','org.pidome.driver.device.rfxcom.rFXComLighting5Device','<device>
    <name>LightwaveRF Relay switch (500W)</name>
    <description>With this device you can control a 500W relay switch.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Open/Close" datatype="boolean" hidden="false" shortcut="1" id="relayswitch">
                <on value="true" label="Open"/>
                <off value="false" label="Close"/>
            </toggle>
			<button id="relaystop" datatype="string" value="relaystop" description="Stop relay" label="Stop" />
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
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiDomeLightwaveRFColorRemote','RFXCom: Lightwave Color remote','org.pidome.driver.device.rfxcom.rFXComLighting5Device','<device>
    <name>LightwaveRF Color remote</name>
    <description>With this device you can control Lightwave RGB fixtures.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Socket lock" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="level" shortcut="2" description="Set level" />
			<button id="colornext" datatype="string" value="colornext" description="Next color" label="Color" />
			<button id="colortone" datatype="string" value="colortone" description="Next color tone" label="Tone" />
			<button id="colorcycle" datatype="string" value="colorcycle" description="Color cycle" label="Cycle" />
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
PRAGMA user_version=14;