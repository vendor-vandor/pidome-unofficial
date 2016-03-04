INSERT INTO installed_packages ('packageid','name','version','active', 'install_base','package_type','author', 'email','website') VALUES ('PIDOME-NATIVE-PRESENCEKEYPAD', 'pidome-keypad', '0.0.1', 1, 'pidome-keypad.jar','mix', 'PiDome','support@pidome.org', 'http://pidome.org');
INSERT INTO installed_drivers ('driverid','name','friendlyname','driver','version','peripheral_driver','package') VALUES ('NATIVE_PIDOME_PRESENCEKEYPAD_DRIVER','PiDome@NativePresenceKeypad','PiDome Presence Keypad driver','org.pidome.driver.driver.nativePiDomePresenceKeypadDriver','0.0.1',(SELECT p.id FROM installed_peripherals p WHERE p.name='FTDIFT232RL' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-PRESENCEKEYPAD' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('pidomekeypadpresencedevice','PiDome Keypad Presence device','org.pidome.driver.device.nativePiDomePresenceKeypadDevice','<device>
    <name>PiDome Keypad device</name>
    <description>A description of what the device does</description>
    <commandset>        
        <group id="settings" label="Keypad settings">
            <toggle description="Toggle sound" datatype="boolean" hidden="false" retention="false" extra="" id="silencetoggle">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
            <toggle description="Toggle edit" datatype="boolean" hidden="false" retention="false" extra="" id="edittoggle">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
        </group>
        <group id="actions" label="Keypad actions">
            <button description="Beep" datatype="string" value="beep" hidden="false" label="Send beep" extra="" id="beep" />
            <button description="Alarm" datatype="string" value="alarm" hidden="false" label="Set alarmed" extra="" id="alarm" />
            <button description="Tampering" datatype="string" value="tamper" hidden="false" label="Set tampered" extra="" id="tamper" />
            <button description="Reset alarm/tamper" datatype="string" value="tamper" hidden="false" label="Reset" extra="" id="resettamperalarm" />
        </group>
        <group id="data" label="Keypad data">
            <data description="Last user id code" datatype="string" hidden="false" id="uidcode" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last user id card" datatype="string" hidden="false" id="uidcard" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
        </group>
    </commandset>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_PIDOME_PRESENCEKEYPAD_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-PRESENCEKEYPAD' LIMIT 1));
PRAGMA user_version=36;