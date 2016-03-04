UPDATE installed_devices SET `xml`='<device>
    <name>AC: Wall Switch</name>
    <description>This device is used to switch for example wall socket devices with the use of the AC protocol.</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remotes address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>' WHERE `name`='ACWallSwitchRFXCom';
UPDATE installed_devices SET `xml`='<device>
    <name>AC: Detection</name>
    <description>This device is used for detection with devices sending information using the AC protocl. This can be doorbells, movement detection, magnet contacts, etc.. This device sends data in the exact same structure as a AC remote device would. Meaning this device acts as a remote</description>
    <commandset>        
		<group id="deviceactions" label="Detection">
			<data description="Movement" datatype="boolean" id="switch" prefix="" suffix="" shortcut="1" visual="move" />
		</group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the motion detector.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>' WHERE `name`='ACDetectionRFXCom';
UPDATE installed_devices SET `xml`='<device>
    <name>AC: Dimmable Wall Socket Switch</name>
    <description>This device is used with wall socket switches which are capable of dimming using the AC protocol</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="boolean" hidden="false" shortcut="1" id="switch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="dimlevel" shortcut="2" description="Set the light dim level" />
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>' WHERE `name`='ACWallSwitchDIMRFXCom';
UPDATE installed_devices SET `xml`='<device>
    <name>AC: Grouped Wall Switches</name>
    <description>This device is used to switch a group of for example wall socket devices with the use of the AC protocol. Allthough not a real device, it can be used as such in the server</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="boolean" hidden="false" shortcut="1" id="groupswitch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>' WHERE `name`='ACGroupedWallSwitchesRFXCom';
UPDATE installed_devices SET `xml`='<device>
    <name>AC: Dimmable Wall Socket Switch</name>
    <description>This device is used with wall socket switches which are capable of dimming using the AC protocol</description>
    <commandset>        
	    <group id="deviceactions" label="Device actions">
            <toggle description="Switch device" datatype="boolean" hidden="false" shortcut="1" id="groupswitch">
                <on value="true" label="On"/>
                <off value="false" label="Off"/>
            </toggle>
			<slider min="0" max="100" id="groupdimlevel" shortcut="2" description="Set the light dim level" />
        </group>
    </commandset>
    <options>
        <text id="subtype" label="RFXCom subtype" description="This is the RFXCom subtype" datatype="string" order="1"/>
    </options>
    <address>
        <description>This is the address of the wall socket. It is combined with the remote address and button order.</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>' WHERE `name`='ACGroupedWallSwitchesDIMRFXCom';
PRAGMA `user_version`=54;