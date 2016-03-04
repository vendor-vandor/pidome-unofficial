UPDATE installed_devices SET `xml`='<device>
    <name>PiDome Tripple reflective Sensor reader</name>
    <description>This device is mostly used to read electric, water and gas consumption usage by checking when a reflective part on a meter passes the sensor. This device should be used in conjunction with the utility measurements plugin. It can be used stand alone as a pulse counter.</description>
    <commandset>
        <group id="sensors" label="Consumption">
            <data id="0x01" datatype="float" description="Data sensor 1" interval="1" intervalcommand="0x01" prefix="" suffix="pulse(s)" graph="time-series" />
            <data id="0x02" datatype="float" description="Data sensor 2" interval="1" intervalcommand="0x02" prefix="" suffix="pulse(s)" graph="time-series" />
            <data id="0x03" datatype="float" description="Data sensor 3" interval="1" intervalcommand="0x03" prefix="" suffix="pulse(s)" graph="time-series" />
        </group>
    </commandset>
	<options>
        <select id="sensor1active" datatype="boolean" description="Should sensor 1 be active?">
		    <option value="true" label="Yes" />
            <option value="false" label="No" />
        </select>
        <select id="sensor2active" datatype="boolean" description="Should sensor 2 be active?">
		    <option value="true" label="Yes" />
            <option value="false" label="No" />
        </select>
        <select id="sensor3active" datatype="boolean" description="Should sensor 3 be active?">
		    <option value="true" label="Yes" />
            <option value="false" label="No" />
        </select>
	</options>
    <address>
        <description>Please give the I²C address of this device.</description>
        <input type="text" datatype="hex" description="The device Address"/>
    </address>
</device>' WHERE `name`='PidomeTrippleReflectorSensorsI2CBoard';
PRAGMA user_version=33;