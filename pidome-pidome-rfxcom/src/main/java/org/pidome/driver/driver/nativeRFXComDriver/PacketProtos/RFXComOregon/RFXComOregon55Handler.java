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
public class RFXComOregon55Handler extends PacketProtocolBase {

    
    Logger LOG = LogManager.getLogger(RFXComOregon55Handler.class);
    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * RGR126/682/918
         */
        TYPE1(1,"RGR126/682/918"),
        /**
         * PCR800
         */
        TYPE2(2,"PCR800"),
        /**
         * TFA
         */
        TYPE3(3,"TFA"),
        /**
         * UPM RG700
         */
        TYPE4(4,"UPM RG700"),
        /**
         * WS2300
         */
        TYPE5(5,"WS2300");
        
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
                put(PacketSubType.TYPE3.getValue(), PacketSubType.TYPE3);
                put(PacketSubType.TYPE4.getValue(), PacketSubType.TYPE4);
                put(PacketSubType.TYPE5.getValue(), PacketSubType.TYPE5);
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
    public RFXComOregon55Handler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
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
    private void handleReceived(RFXComOregon55Handler.PacketSubType subType, byte[] bytes) throws PacketProtocolException {
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<2;i++){
            builder.append(RFXComBasicPacketParser.decodeSingleByte(bytes[i]));
        }
        String deviceId = builder.toString();
        
        int lowRate  = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[2]),16);
        int highRate = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[3]),16);
        
        float currentRate = 0;
        
        switch(subType){
            case TYPE1:
                currentRate = (highRate * 0x100) + lowRate;
            break;
            case TYPE2:
                currentRate = ((highRate * 0x100) + lowRate ) / 100;
            break;
        }
        
        int totalRate1 = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[4]),16);
        int totalRate2 = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[5]),16);
        int totalRate3 = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[6]),16);
        
        float totalRate = ( (totalRate1 * 0x1000) + (totalRate2 * 0x100) + totalRate3 ) / 10;
        
        int batSig = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[7]),16);
        
        double bat = RFXComOregonTools.getBatteryStatus(batSig);
        double sig = RFXComOregonTools.getSignalStatus(batSig);
        
        boolean found = false;
        
        LOG.debug("Type: {}, Address: {}, Current rate: {} mm/h, Total rate: {} mm, Battery: {}, Signal: {}", subType.getDescription(), deviceId, currentRate, totalRate, bat, sig);
        
        for(Device device:this.getDriver().getRunningDevices()){
            if (device.getAddress().equals(deviceId)){
                found = true;
                RFXComOregonCommand command = new RFXComOregonCommand();
                command.setTotalRainRate(totalRate);
                command.setCurrentRainRate(currentRate);
                command.setBattery(bat);
                command.setSignal(sig);
                ((RFXComDevice)device).handleData(command);
            }
        }
        if(!found && DiscoveredItemsCollection.discoveryEnabled(this.getDriver())){
            
            DiscoveredDevice newDevice = new DiscoveredDevice(deviceId,new StringBuilder(subType.getDescription()).append(" or alike").toString());
            
            newDevice.addVisualInformation("Supplied current", currentRate);
            newDevice.addVisualInformation("Supplied total",totalRate);
            newDevice.addVisualInformation("Provides", "Rain information");
            newDevice.addVisualInformation("packettype", RFXComDefinitions.PacketType.OREGON55.toString());
            newDevice.addVisualInformation("packetsubtype", subType.toString());
            
            newDevice.addParameterValue("packettype", RFXComDefinitions.PacketType.OREGON55.toString());
            newDevice.addParameterValue("packetsubtype", subType.toString());
            
            newDevice.setDeviceDriver("org.pidome.driver.device.rfxcom.rFXComOregon55Device");
            
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
