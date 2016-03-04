UPDATE installed_devices SET `xml`='<device>
    <name>PiDome Keypad device</name>
    <description>This device is used to handle access by using a NFC token.</description>
    <commandset>
        <group id="env" label="Environment">
            <data description="Temperature" datatype="float" hidden="false" id="temp" retention="false" extra="" visual="temperature" prefix="" suffix="C" graph="time-series" minvalue="-10" maxvalue="60" warnvalue="32" highvalue="40"/>
            <data description="Light" datatype="float" hidden="false" id="lux" retention="false" extra="" visual="luxlevel" prefix="" suffix="Lux" graph="time-log"/>
        </group>
        <group id="status" label="Keypad status">
            <data description="Alarming" datatype="boolean" hidden="false" id="alarming" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last user" datatype="string" hidden="false" id="lastuser" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Current status" datatype="string" hidden="false" id="curstatus" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
        </group>  
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
</device>' WHERE `name`='pidomenfcpresencedevice';
PRAGMA `user_version`=50;