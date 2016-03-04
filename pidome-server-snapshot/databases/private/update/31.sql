UPDATE installed_devices SET `xml`='<device>
    <name>PiDome Server</name>
    <description>Some server data. This data is calculated system wide. Except for the memory usage which is from the PiDome server application</description>
    <commandset>
        <group id="values" label="Device values">
            <data description="Memory usage" datatype="float" id="memusage" value="memusage" prefix="" suffix="MB" shortcut="2" visual="pcvalue" graph="time-series"/>
            <data description="Disk space" datatype="float" id="diskspace" value="diskspace" prefix="" suffix="MB" visual="pcvalue" graph="time-series"/>
            <data description="CPU load" datatype="float" id="cpuusage" value="cpuusage" prefix="" suffix="%" shortcut="1" visual="pcvalue" graph="time-series"/>
            <data description="Pi temperature" datatype="float" id="procheat" value="procheat" prefix="" suffix="°C" visual="pcvalue" graph="time-series"/>
        </group>
    </commandset>
</device>' WHERE `name`='pidomeServerDevice';
UPDATE installed_devices SET `xml`='<device>
    <name>I2C temp/ldr/NeoPixel ledstrip (I2C-LTS-LC)</name>
    <description>breadboard setup.</description>
    <commandset>
        <group id="values" label="Device values">
            <data description="Temperature" datatype="float" id="0x01" interval="1" intervalcommand="0x01" prefix="" suffix="°C" shortcut="1" graph="time-series" visual="temperature" />
            <data description="Light intensity" datatype="float" id="0x06" interval="1" intervalcommand="0x06" prefix="" suffix="Lux" graph="time-series:log" visual="luxlevel"/>
        </group>
        <group id="actions" label="Device control">
            <toggle datatype="string" description="Turn light On/Off" id="switch" shortcut="2">
                <on value="y" label="On"/>
                <off value="z" label="Off"/>
            </toggle>
            <toggle description="Light color follows temperature" id="flTemp">
                <on value="ttlOn" label="On"/>
                <off value="ttlOff" label="Off"/>
            </toggle>
            <slider min="0" max="255" id="t" description="Effects speed" />
            <button id="curstoresettings" datatype="string" value="s" description="Save current light as default" label="Store" />
        </group>
        <group id="moodcolor" label="Color selection">
            <colorpicker id="rgb" mode="hsb" description="Main light" shortcut="3">
                <button value="n" label="Set directly"/>
                <button value="c" label="Color fade"/>
                <button value="x" label="Fade random leds"/>
                <button value="l" label="Wipe from left to right"/>
                <button value="r" label="Wipe from right to left"/>
            </colorpicker>
        </group>
    </commandset>
    <address>
        <description>An I²C addres is like 0x40. This is also the default address for this device. So if you have not changed it, use this.</description>
        <input type="text" datatype="hex" description="The device Address"/>
    </address>
</device>' WHERE `name`='i2cLtsLc';
UPDATE installed_devices SET `xml`='<device>
    <name>Temperature sensor</name>
    <description>Sensor supplying temperature</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" visual="temperature" graph="time-series"/>
        </group>
        <group id="device" label="Device status">
            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%" graph="time-series" visual="battery" />
            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix="" graph="time-series"/>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="OREGON" label="Oregon protocol" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="TYPE1" label="THR128/138, THC138" />
			<option value="TYPE2" label="THC238/268,THN132,THWR288,THRN122,THN122,AW129/131" />
			<option value="TYPE3" label="THWR800" />
			<option value="TYPE4" label="RTHN318" />
			<option value="TYPE5" label="La Crosse TX3, TX4, TX17" />
			<option value="TYPE6" label="TS15C" />
			<option value="TYPE7" label="Viking 02811" />
			<option value="TYPE8" label="La Crosse WS2300La" />
			<option value="TYPE9" label="RUBiCSON" />
			<option value="TYPE10" label="TFA 30.3133" />
        </select>
    </options>
    <address>
        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>
        <input type="text" datatype="string" description="Sensor address"/>
    </address>
</device>' WHERE `name`='PiDomeRFXComTempSensor';
UPDATE installed_devices SET `xml`='<device>    <name>Humidity sensor</name>    <description>Sensor Humidity data</description>    <commandset>        <group id="environment" label="Environment">			<data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" shortcut="1" visual="humidity" graph="time-series"/>			<data description="Humidity status" datatype="string" id="humiditystat" value="humiditystat" visual="humidity" prefix="" suffix=""/>        </group>        <group id="device" label="Device status">            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%" visual="battery" graph="time-series"/>            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix="" graph="time-series"/>        </group>    </commandset></device>' WHERE `name`='PiDomeRFXComHumidSensor';
UPDATE installed_devices SET `xml`='<device>
    <name>Temperature and humidity sensor</name>
    <description>Sensor supplying temperature and humidity</description>
    <commandset>
        <group id="environment" label="Environment">
            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" graph="time-series" visual="temperature" />
            <data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" shortcut="2" graph="time-series" visual="humidity" />	
            <data description="Humidity status" datatype="string" id="humiditystat" value="humiditystat" prefix="" suffix="" visual="humidity" />
        </group>
        <group id="device" label="Device status">
            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%" graph="time-series" visual="battery" />
            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix="" graph="time-series"/>
        </group>
    </commandset>
    <options>
        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">
            <option value="OREGON52" label="Oregon(52)" />
        </select>
        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">
            <option value="TYPE1" label="Type1" />
            <option value="TYPE2" label="Type1" />
            <option value="TYPE3" label="Type1" />
            <option value="TYPE4" label="Type1" />
            <option value="TYPE5" label="Type1" />
            <option value="TYPE6" label="Type1" />
            <option value="TYPE7" label="Type1" />
            <option value="TYPE8" label="Type1" />
            <option value="TYPE9" label="Type1" />
            <option value="TYPE10" label="Type1" />
            <option value="TYPE1" label="Type1" />
        </select>
    </options>
    <address>
        <description>This is the address of the wall socket. Structure is 00,00,00:0 (hex,hex,hex:number). The hex values are the device id and the number is the unit code</description>
        <input type="text" datatype="string" description="Device address"/>
    </address>
</device>' WHERE `name`='PiDomeRFXComTempHumSensor';
UPDATE installed_devices SET `xml`='<device>    <name>Temperature, humidity and barometric sensor</name>    <description>Sensor supplying temperature, humidity and barometric data</description>    <commandset>        <group id="environment" label="Environment">            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" visual="temperature" graph="time-series"/>			<data description="Humidity" datatype="float" id="humidity" value="humidity" prefix="" suffix="%" shortcut="2" visual="humidity" graph="time-series"/>			<data description="Humidity status" datatype="string" id="humiditystat" value="humiditystat" visual="humidity" prefix="" suffix=""/>			<data description="Pressure" datatype="float" id="pressure" value="pressure" prefix="" suffix="" visual="pressure" shortcut="3" graph="time-series"/>			<data description="Forecast" datatype="string" id="forecast" value="forecast" prefix="" suffix=""/>        </group>        <group id="device" label="Device status">            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" suffix="%" visual="battery" graph="time-series"/>            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix="" graph="time-series"/>        </group>    </commandset></device>' WHERE `name`='PiDomeRFXComTempHumidBarSensor';
UPDATE installed_devices SET `xml`='<device>    <name>Rain sensor</name>    <description>Sensor supplying rain data</description>    <commandset>        <group id="environment" label="Environment">            <data description="Current" datatype="float" id="raincurrent" value="raincurrent" prefix="" suffix="" shortcut="1" visual="fluid" graph="time-series" />            <data description="Total" datatype="float" id="raintotal" value="raintotal" prefix="" suffix="" shortcut="2" visual="fluid" graph="time-totals" />        </group>        <group id="device" label="Device status">            <data description="Battery" datatype="float" id="battery" value="battery" prefix="" visual="battery" suffix="%"/>            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix=""/>        </group>    </commandset>    <options>        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">            <option value="OREGON" label="Oregon protocol" />        </select>        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">            <option value="TYPE1" label="RGR126/682/918" />            <option value="TYPE2" label="PCR800" />            <option value="TYPE3" label="TFA" />            <option value="TYPE4" label="UPM RG700" />            <option value="TYPE5" label="WS2300" />        </select>    </options>    <address>        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>        <input type="text" datatype="string" description="Sensor address"/>    </address></device>' WHERE `name`='PiDomeRFXComRainSensor';
UPDATE installed_devices SET `xml`='<device>    <name>Wind sensor</name>    <description>Sensor supplying wind and temperature data</description>    <commandset>        <group id="environment" label="Environment">            <data description="Wind speed" datatype="float" id="windspeed" value="windspeed" prefix="" suffix="" shortcut="1" visual="wind" graph="time-series" />            <data description="Wind direction" datatype="float" id="winddirection" value="winddirection" prefix="" suffix="" visual="wind" shortcut="2" />            <data description="Wind gust" datatype="float" id="windgust" value="windgust" prefix="" suffix="" visual="wind" shortcut="3" />            <data description="Wind chill" datatype="float" id="windchill" value="windchill" visual="temperature" prefix="" suffix="°C" />            <data description="Temperature" datatype="float" id="temperature" value="temperature" prefix="" suffix="°C" shortcut="1" graph="time-series" visual="temperature"/>        </group>        <group id="device" label="Device status">            <data description="Battery" datatype="float" id="battery" value="battery" visual="battery" prefix="" suffix="%"/>            <data description="Signal" datatype="float" id="signal" value="signal" prefix="" suffix=""/>        </group>    </commandset>    <options>        <select id="type" datatype="string" label="Preset" description="RFXCom protocol" order="1">            <option value="OREGON" label="Oregon protocol" />        </select>        <select id="subtype" datatype="string" label="Preset" description="RFXCom sub protocol" order="2">            <option value="TYPE1" label="WTGR800" />            <option value="TYPE2" label="WGR800" />            <option value="TYPE3" label="STR918, WGR918" />            <option value="TYPE4" label="TFA (WIND4)" />            <option value="TYPE5" label="UPM WDS500" />            <option value="TYPE6" label="WS2300" />        </select>    </options>    <address>        <description>This is the address of the sensor. This are two hex values combined to one. Please refer to the rfxcom documentation to find out the address. It is easier to use the auto discovery feature of the driver</description>        <input type="text" datatype="string" description="Sensor address"/>    </address></device>', `name`='PiDomeRFXComWindSensor' WHERE `name`='PiDomeRFXComRainSensor' and `driver`='org.pidome.driver.device.rfxcom.rFXComOregon56Device';
PRAGMA user_version=31;