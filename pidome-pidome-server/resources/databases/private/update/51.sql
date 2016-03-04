UPDATE installed_devices SET `xml`='<device>
    <name>PiDome Keypad device</name>
    <description>This device is used to handle access by using a NFC token.</description>
    <commandset>
        <group id="env" label="Environment">
            <data description="Temperature" datatype="float" hidden="false" id="temp" retention="false" extra="" visual="temperature" prefix="" suffix="C" graph="time-series" minvalue="-10" maxvalue="60" warnvalue="32" highvalue="40"/>
            <data description="Light" datatype="float" hidden="false" id="lux" retention="false" extra="" visual="luxlevel" prefix="" suffix="Lux" graph="time-log"/>
        </group>
        <group id="status" label="Keypad status">
            <data description="Last user" datatype="string" hidden="false" id="lastuser" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Current status" datatype="string" hidden="false" id="curstatus" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
        </group>  
        <group id="actions" label="Keypad actions">
            <button description="Beep" datatype="string" value="beep" label="Send beep" extra="" id="beep"  hidden="true"/>
            <button description="Alarm" datatype="string" value="alarm" label="Set alarmed" extra="" id="alarm"  hidden="true"/>
            <button description="Tampering" datatype="string" value="tamper" label="Set tampered" extra="" id="tamper" hidden="true"/>
            <button description="Reset alarm/tamper" datatype="string" value="tamper" label="Reset" extra="" id="resettamperalarm" />
            <toggle description="Toggle edit" datatype="boolean" retention="false" extra="" id="edittoggle" hidden="true">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
        </group>
        <group id="data" label="Keypad data">
            <data description="Last message" datatype="string" hidden="false" id="message" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last error" datatype="string" hidden="false" id="error" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
        </group>  
    </commandset>
</device>' WHERE `name`='pidomenfcpresencedevice';
PRAGMA `user_version`=51;