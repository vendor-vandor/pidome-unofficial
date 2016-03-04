CREATE TABLE [tokensets] (
  [id] INTEGER PRIMARY KEY AUTOINCREMENT, 
  [person] INTEGER NOT NULL CONSTRAINT [person_token_link] REFERENCES [persons]([id]) ON DELETE CASCADE ON UPDATE CASCADE, 
  [tokentype] TEXT NOT NULL, 
  [content] BLOB NOT NULL);
CREATE TABLE [tokendevices] (
  [id] INTEGER PRIMARY KEY AUTOINCREMENT, 
  [device] INTEGER NOT NULL CONSTRAINT [tokendevices_device_link] REFERENCES [devices]([id]) ON DELETE CASCADE ON UPDATE CASCADE);
CREATE TABLE [tokenset_has_devices] (
  [tokenset] INTEGER NOT NULL CONSTRAINT [tokensets_link] REFERENCES [tokensets]([id]) ON DELETE CASCADE ON UPDATE CASCADE, 
  [tokendevice] INTEGER NOT NULL CONSTRAINT [tokendevices_link] REFERENCES [tokendevices]([id]) ON DELETE CASCADE ON UPDATE CASCADE);
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
            <data description="Last user id code" datatype="string" hidden="false" id="uidcode" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last user id card" datatype="string" hidden="false" id="uidcard" retention="false" extra="" visual="none" prefix="" suffix="" graph="none"/>
            <data description="Last message" datatype="string" id="message" prefix="" suffix="" visual="none" graph="none" readonly="false" />
        </group>
    </commandset>
</device>' WHERE `name`='pidomekeypadpresencedevice';
pragma user_version=46;