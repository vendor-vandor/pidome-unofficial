INSERT INTO installed_packages ('packageid','name','version','active', 'install_base','package_type','author', 'email','website') VALUES ('PIDOME-PIREMOTE-ISH-PACKAGE', 'PiRemoteIsh', '0.0.1', 1, 'PiRemoteIsh.jar','driver', 'PiDome','support@pidome.org', 'http://pidome.org');
INSERT INTO installed_drivers ('driverid','name','friendlyname','driver','version','peripheral_driver','package') VALUES ('NATIVE_PIREMOTE-ISH_DRIVER','PiDome@PiRemote','PiRemote driver','org.pidome.driver.driver.piRemoteishDriver','0.0.1',(SELECT p.id FROM installed_peripherals p WHERE p.name='FTDIFT232RL' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-PIREMOTE-ISH-PACKAGE' LIMIT 1));
INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('PiRemoteNodeDevice','PiRemote Node','org.pidome.driver.device.piRemoteishDevice','<device>
    <name>PiRemote device node</name>
    <description>This is a custom created device (PiRemote-ish device)</description>
    <commandset>        
        <group id="deviceactions" label="Device actions">
            <toggle description="Switch lamp" datatype="string" hidden="false" shortcut="1" id="lampswitch">
                <on value="P" label="On"/>
                <off value="p" label="Off"/>
            </toggle>
            <toggle description="Switch led" datatype="string" hidden="false" shortcut="2" id="ledswitch">
                <on value="L" label="On"/>
                <off value="l" label="Off"/>
            </toggle>
        </group>
        <group id="devicesettings" label="Device settings">
            <toggle description="Manual/Auto" datatype="string" hidden="false" id="automanual">
                <on value="A" label="Auto"/>
                <off value="a" label="Manual"/>
            </toggle>
        </group>
        <group id="environment" label="Environment">
            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="3" graph="time-series" visual="temperature"/>
            <data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" graph="time-series"/>
            <data description="Voltage" datatype="float" id="voltage" value="voltage" prefix="" suffix="V" graph="time-series"/>
            <data description="Current" datatype="float" id="current" value="current" prefix="" suffix="A" graph="time-series"/>
        </group>
    </commandset>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_PIREMOTE-ISH_DRIVER' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-PIREMOTE-ISH-PACKAGE' LIMIT 1));
PRAGMA user_version=20;