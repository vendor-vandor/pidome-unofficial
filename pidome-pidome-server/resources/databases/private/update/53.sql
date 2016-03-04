UPDATE installed_devices SET `xml`='<device>
    <name>PiDome Server</name>
    <description>Some server data. This data is calculated system wide. Except for the memory usage which is from the PiDome server application</description>
    <commandset>
        <group id="values" label="Device values">
            <data description="Uptime" datatype="string" id="uptime" value="uptime" prefix="" suffix="" shortcut="0" visual="" graph="none" />
            <data description="Memory usage" datatype="float" id="memusage" value="memusage" prefix="" suffix="MB" shortcut="2" visual="pcvalue" graph="time-series" minvalue="0" maxvalue="120" warnvalue="50" highvalue="75" />
            <data description="Disk space" datatype="float" id="diskspace" value="diskspace" prefix="" suffix="MB" visual="pcvalue" graph="time-series"/>
            <data description="CPU load" datatype="float" id="cpuusage" value="cpuusage" prefix="" suffix="%" shortcut="1" visual="pcvalue" graph="time-series" minvalue="0" maxvalue="100" warnvalue="50" highvalue="75"/>
            <data description="Pi temperature" datatype="float" id="procheat" value="procheat" prefix="" suffix="°C" visual="pcvalue" graph="time-series" minvalue="0" maxvalue="100" warnvalue="60" highvalue="80"/>
        </group>
    </commandset>
</device>' WHERE `name`='pidomeServerDevice';
PRAGMA `user_version`=53;