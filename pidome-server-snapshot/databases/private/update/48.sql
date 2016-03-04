UPDATE installed_devices SET `xml`='<device>
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
            <button description="Reboot" datatype="string" value="reboot" hidden="false" label="Reboot" extra="" id="reboot" />
            <button description="Reset" datatype="string" value="reset" hidden="false" label="reset" extra="" id="reset" />
        </group>
        <group id="data" label="Keypad data">
            <data description="Last message" datatype="string" hidden="false" id="message" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last error" datatype="string" hidden="false" id="error" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
        </group>
    </commandset>
</device>' WHERE `name`='pidomekeypadpresencedevice';
pragma user_version=48;