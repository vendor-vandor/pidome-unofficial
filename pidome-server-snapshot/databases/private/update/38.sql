UPDATE installed_devices SET `xml`='<device>
    <name>BlinkM Smart RGB led</name>
    <description>BlinkM leds from ThingM are powerfull little rgb leds, more information at http://thingm.com/products/blinkm. We do not support all the BlinkM functionalities, there are a lot more. This xml is for demonstration purposes to show how easy it is to add an I2C device.</description>
    <commandset>
        <group id="scriptactions" label="Script actions">
            <select description="Choose script to run" id="runscripts" datatype="hex">
                <option value="0x70 0x00 0x00 0x00" label="Start up script"/>
                <option value="0x70 0x01 0x00 0x00" label="RGB loop"/>
                <option value="0x70 0x02 0x00 0x00" label="Pulse white"/>
                <option value="0x70 0x03 0x00 0x00" label="Pulse red"/>
                <option value="0x70 0x04 0x00 0x00" label="Pulse green"/>
                <option value="0x70 0x05 0x00 0x00" label="Pulse blue"/>
                <option value="0x70 0x06 0x00 0x00" label="Pulse cyan"/>
                <option value="0x70 0x07 0x00 0x00" label="Pulse magenta"/>
                <option value="0x70 0x08 0x00 0x00" label="Pulse yellow"/>
                <option value="0x70 0x0a 0x00 0x00" label="Hue cycle"/>
                <option value="0x70 0x0b 0x00 0x00" label="Mood light"/>
                <option value="0x70 0x0c 0x00 0x00" label="Virtual candle"/>
                <option value="0x70 0x0d 0x00 0x00" label="Water reflections"/>
                <option value="0x70 0x0e 0x00 0x00" label="Broken old neon"/>
                <option value="0x70 0x0f 0x00 0x00" label="The 4 seasons"/>
                <option value="0x70 0x10 0x00 0x00" label="Thunderstorm"/>
                <option value="0x70 0x11 0x00 0x00" label="Traffic light"/>
                <option value="0x70 0x12 0x00 0x00" label="S.O.S. Morse"/>
            </select>
            <button id="blackout" value="0x70 0x09 0x00 0x00" datatype="hex" label="Blackout" description="Blackout"/>
            <button id="scriptstop"  value="0x6f" datatype="hex" label="Stop script" description="Stop current running script"/>
        </group>
        <group id="devicesettings" label="Device Settings">
            <slider min="1" max="255" id="0x66" description="Set fade speed (higher is faster)" datatype="integer"/>
            <slider min="-128" max="127" id="0x74" description="Set time adjust (higher is slower)" datatype="integer"/>
        </group>
        <group id="moodcolor" label="Color selection">
            <colorpicker id="rgbselect" mode="hsb" datatype="color" description="Main light" shortcut="3">
                <button value="0x6e" label="Set"/>
                <button value="0x63" label="Fade to"/>
            </colorpicker>
        </group>
    </commandset>
    <address>
        <description>The default BlinkM address is 0x09, check your BlinkM address or follow the instructions in the datasheet at http://blinkm.thingm.com. Do not use 0x00 known as broadcast/common address, it is unsupported on the raspberry pi!</description>
        <input type="text" id="address" datatype="hex" description="The device address"/>
    </address>
</device>' WHERE `name`='blinkmsmartled';
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
            <slider min="0" max="255" id="t" description="Effects speed" datatype="integer" />
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
    <name>Philips Hue</name>
    <description>This device can be used with all the Philips Hue devices supporting multiple colors like the default bulbs, bloom, iris and LED strips.</description>
    <commandset>
        <group id="lightactions" label="Light actions">
		    <toggle id="onoff" shortcut="0" datatype="boolean" description="Toggle device on/off">
			    <on value="true" label="On"/>
				<off value="false" label="Off"/>
			</toggle>
			<colorpicker id="setcolor" datatype="color" mode="hsb" description="Main light" shortcut="1" label="Pick a color and Set">
				<button value="setcolorvalue" label="Set color"/>
			</colorpicker>
		</group>
		<group id="otheractions" label="Other actions">
			<button id="sendalert" datatype="string" value="select" description="Send a single alert" label="Send alert" />
		</group>
		<group id="deviceoptions" label="Device options">
			<slider min="0" max="100" id="transtime" description="Transition time (in tenth of a second)" datatype="integer" />
		</group>
	</commandset>
    <address>
        <description>Fill in the light number of this bulb</description>
        <input type="text" datatype="integer" description="The device Address"/>
    </address>
</device>' WHERE `name`='pidomeNativeHueDevice';
UPDATE installed_devices SET `xml`='<device>
    <name>Philips Hue Lux</name>
    <description>A Philips Hue light bulb which delivers only white colors.</description>
    <commandset>
        <group id="lightactions" label="Light actions">
		    <toggle id="onoff" shortcut="0" datatype="boolean" description="Toggle device on/off">
			    <on value="true" label="On"/>
				<off value="false" label="Off"/>
			</toggle>
			<slider min="0" max="100" id="brightness" description="Set the light brightness" datatype="integer" />
		</group>
		<group id="otheractions" label="Other actions">
			<button id="sendalert" datatype="string" value="select" description="Send a single alert" label="Send alert" />
		</group>
		<group id="deviceoptions" label="Device options">
			<slider min="0" max="100" id="transtime" description="Transition time (in tenth of a second)" datatype="integer" />
		</group>
	</commandset>
    <address>
        <description>Fill in the light number of this bulb</description>
        <input type="text" datatype="integer" description="The device Address"/>
    </address>
</device>' WHERE `name`='pidomeNativeHueLuxDevice';
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
        </group>
    </commandset>
</device>' WHERE `name`='pidomekeypadpresencedevice';
PRAGMA user_version=38;