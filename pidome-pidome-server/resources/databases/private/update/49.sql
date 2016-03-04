INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('pidomenfcpresencedevice','PiDome NFC presence device','org.pidome.driver.device.nativePiDomePresenceNFCDevice','<device>
    <name>PiDome Keypad device</name>
    <description>A description of what the device does</description>
    <commandset>        
        <group id="actions" label="Keypad actions">
            <button description="Beep" datatype="string" value="beep" hidden="false" label="Send beep" extra="" id="beep" />
            <button description="Alarm" datatype="string" value="alarm" hidden="false" label="Set alarmed" extra="" id="alarm" />
            <button description="Tampering" datatype="string" value="tamper" hidden="false" label="Set tampered" extra="" id="tamper" />
            <button description="Reset alarm/tamper" datatype="string" value="tamper" hidden="false" label="Reset" extra="" id="resettamperalarm" />
        </group>
        <group id="data" label="Keypad data">
            <data description="Last message" datatype="string" hidden="false" id="message" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last error" datatype="string" hidden="false" id="error" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
        </group>
    </commandset>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_PIDOME_PRESENCEKEYPAD_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-PRESENCEKEYPAD' LIMIT 1));
PRAGMA `user_version`=49;