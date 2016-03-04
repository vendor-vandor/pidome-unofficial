/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.NativeRFXComDriver;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolBase;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.RFXComBasicPacketParser;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;

/**
 *
 * @author John
 */
public class RFXComOregon54Handler extends PacketProtocolBase {

    
    Logger LOG = LogManager.getLogger(RFXComOregon54Handler.class);
    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * BTHR918
         */
        TYPE1(1,"BTHR918"),
        /**
         * BTHR918N, BTHR968
         */
        TYPE2(2,"BTHR918N, BTHR968");
        
        /**
         * Supplying value.
         */
        private final int value;
        private final String desc;
        
        /**
         * Enum constructor for supplying a value to the enum.
         * @param newValue 
         */
        private PacketSubType(final int value, final String desc) {
            this.value = value;
            this.desc = desc;
        }

        /**
         * Returns the enum value;
         * @return 
         */
        public int getValue() { return value; }
        
        public String getDescription() { return desc; }
        
    }
    
    /**
     * Base protocol mapper.
     */
    private final static Map<Integer,PacketSubType> subPacketTypes = Collections.unmodifiableMap(new HashMap<Integer,PacketSubType>(){{
                put(PacketSubType.TYPE1.getValue(), PacketSubType.TYPE1);
                put(PacketSubType.TYPE2.getValue(), PacketSubType.TYPE2);
            }});
    
    /**
     * Returns a subpacket type indexed by the byte's int value.
     * @return 
     */
    public static Map<Integer,PacketSubType> getSubPacketTypes(){
        return subPacketTypes;
    }
    
    /**
     * Returns a sub packet type by String id.
     * @param id
     * @return 
     */
    public static int getSubPacketTypeIntByStringId(String id){
        for(Map.Entry<Integer,PacketSubType> type:subPacketTypes.entrySet()){
            if(type.getValue().toString().equals(id)){
                return type.getKey();
            }
        }
        return 255;
    }
    
    /**
     * Constructor.
     * @param driver
     * @param parser 
     */
    public RFXComOregon54Handler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
        super(driver, parser);
    }

    /**
     * Handles the received data.
     * @throws PacketProtocolException 
     */
    @Override
    public final void handleReceived() throws PacketProtocolException {
        handleReceived(getSubPacketTypes().get(getParser().getSubType()),this.getParser().getMessageBody());
    }

    /**
     * Handles received packages.
     * @param subType
     * @param bytes
     * @throws PacketProtocolException 
     */
    private void handleReceived(RFXComOregon54Handler.PacketSubType subType, byte[] bytes) throws PacketProtocolException {
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<2;i++){
            builder.append(RFXComBasicPacketParser.decodeSingleByte(bytes[i]));
        }
        
        String deviceId = builder.toString();
        int remoteChannel = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[1]),16);
        double temp = RFXComOregonTools.getTemperature(bytes[2], bytes[3]);
        
        int hum = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[4]),16);
        int humstat = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[5]),16);

        double barLevel = RFXComOregonTools.getPressure(bytes[6], bytes[7]);
        int forecast    = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[8]),16);
        
        int batSig = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[9]),16);
        
        double bat = RFXComOregonTools.getBatteryStatus(batSig);
        double sig = RFXComOregonTools.getSignalStatus(batSig);
        
        boolean found = false;
        
        LOG.debug("Address: {}, Channel: {}, Temp: {}, Humidity: {}, Humidity txt const: {}, Pressure: {}, forecast: {}, Battery: {}, Signal: {}", deviceId, remoteChannel, temp, hum, humstat,barLevel,forecast,bat,sig);
        
        for(Device device:this.getDriver().getRunningDevices()){
            if (device.getAddress().equals(deviceId)){
                found = true;
                RFXComOregonCommand command = new RFXComOregonCommand();
                command.setTemperature(temp);
                command.setHumidity(hum);
                command.setHumidityText(RFXComDefinitions.PacketType.OREGON54,humstat);
                command.setBattery(bat);
                command.setSignal(sig);
                command.setPressure(barLevel);
                command.setForecastText(RFXComDefinitions.PacketType.OREGON54, forecast);
                ((RFXComDevice)device).handleData(command);
            }
        }
        if(!found && DiscoveredItemsCollection.discoveryEnabled(this.getDriver())){
            
            DiscoveredDevice newDevice = new DiscoveredDevice(deviceId,new StringBuilder(subType.getDescription()).append(" or alike").toString());
            
            newDevice.addVisualInformation("Channel",remoteChannel);
            newDevice.addVisualInformation("Supplied temp", temp);
            newDevice.addVisualInformation("Supplied humudity",hum);
            newDevice.addVisualInformation("Supplied pressure",barLevel);
            newDevice.addVisualInformation("Provides", "Temperature, humidity and pressure");
            newDevice.addVisualInformation("packettype", RFXComDefinitions.PacketType.OREGON54.toString());
            newDevice.addVisualInformation("packetsubtype", subType.toString());
            
            newDevice.addParameterValue("packettype", RFXComDefinitions.PacketType.OREGON54.toString());
            newDevice.addParameterValue("packetsubtype", subType.toString());
            
            newDevice.setDeviceDriver("org.pidome.driver.device.rfxcom.rFXComOregon54Device");
            
            try {
                DiscoveredItemsCollection.addDiscoveredDevice(this.getDriver(), newDevice);
            } catch (DeviceDiscoveryServiceException ex) {
                LOG.warn("Could not add device to discovery table: {}", ex.getMessage());
            }
            
        }
    }
    
    @Override
    public byte[] getSend() throws PacketProtocolException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
