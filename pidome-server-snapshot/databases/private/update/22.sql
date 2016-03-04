INSERT INTO "installed_devices" ('name','friendlyname','driver','xml','selectable','type','struct','version','driver_driver','package') VALUES ('MySensors433RelayDevice','MySensors 433Mhz relay device','org.pidome.driver.device.vanDenBosche433MySensorsDevice','<device>
   <name>MySensors 433 Relay</name>
   <description>This device is used to relay 433Mhz using a MySensors 2.4Ghz device.</description>
   <commandset>
       <group id="deviceactions" label="Device actions">
           <toggle id="switchdevice" shortcut="1" datatype="integer" description="Switch On/Off">
               <on value="1" label="On"/>
               <off value="0" label="Off"/>
           </toggle>
       </group>
   </commandset>
   <address>
       <description>This is the address of the MySensors device.</description>
       <input type="text" datatype="integer" description="Device address"/>
   </address>
   <options>
       <select id="optionProtocol" datatype="integer" label="Preset" description="Select the device type" order="1">
           <option value="0" label="Action" />
           <option value="1" label="Blokker" />
           <option value="2" label="KaKu non learning" />
           <option value="3" label="KaKu learning" />
           <option value="4" label="Elro" />
           <option value="5" label="Chacon" />
       </select>
       <text id="optionHouseCode" label="House code" description="The house code is between a range of A to mostly P" datatype="string" order="2"/>
       <text id="optionDeviceCode" label="Device code" description="The device code correspondents mostly to the dip switches 6 to 10 at the back of the wall plug which are mostly the letters A to E on the remote." datatype="string" order="3"/>
   </options>
</device>',1,0,'{"type":0}','0.0.1',(SELECT id FROM installed_drivers WHERE driverid='NATIVE_PIDOMEMYSENSORSDRIVER_1_4' LIMIT 1),(SELECT id FROM installed_packages WHERE packageid='PIDOME-NATIVE-MYSENSORS-PACKAGE' LIMIT 1));
PRAGMA user_version=22;