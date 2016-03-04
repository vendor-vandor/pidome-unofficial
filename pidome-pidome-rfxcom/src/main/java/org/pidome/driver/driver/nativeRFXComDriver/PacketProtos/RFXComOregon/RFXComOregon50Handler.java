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
public class RFXComOregon50Handler extends PacketProtocolBase {

    
    Logger LOG = LogManager.getLogger(RFXComOregon50Handler.class);
    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * THR128/138, THC138
         */
        TYPE1(1,"THR128/138, THC138"),
        /**
         * THC238/268,THN132,THWR288,THRN122,THN122,AW129/131
         */
        TYPE2(2,"THC238/268,THN132,THWR288,THRN122,THN122,AW129/131"),
        /**
         * THWR800
         */
        TYPE3(3,"THWR800"),
        /**
         * RTHN318
         */
        TYPE4(4,"RTHN318"),
        /**
         * La Crosse TX3, TX4, TX17
         */
        TYPE5(5,"La Crosse TX3, TX4, TX17"),
        /**
         * TS15C
         */
        TYPE6(6,"TS15C"),
        /**
         * Viking 02811
         */
        TYPE7(7,"Viking 02811"),
        /**
         * La Crosse WS2300La
         */
        TYPE8(8,"La Crosse WS2300La"),
        /**
         * RUBiCSON
         */
        TYPE9(9,"RUBiCSON"),
        /**
         * TFA 30.3133
         */
        TYPE10(10,"TFA 30.3133");
        
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
                put(PacketSubType.TYPE6.getValue(), PacketSubType.TYPE6);
                put(PacketSubType.TYPE7.getValue(), PacketSubType.TYPE7);
                put(PacketSubType.TYPE8.getValue(), PacketSubType.TYPE8);
                put(PacketSubType.TYPE9.getValue(), PacketSubType.TYPE9);
                put(PacketSubType.TYPE10.getValue(), PacketSubType.TYPE10);
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
    public RFXComOregon50Handler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
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
    private void handleReceived(RFXComOregon50Handler.PacketSubType subType, byte[] bytes) throws PacketProtocolException {
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<2;i++){
            builder.append(RFXComBasicPacketParser.decodeSingleByte(bytes[i]));
        }
        
        String deviceId = builder.toString();
        int remoteChannel = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[1]),16);
        double temp = RFXComOregonTools.getTemperature(bytes[2], bytes[3]);
        int batSig = Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(bytes[4]),16);
        double bat = RFXComOregonTools.getBatteryStatus(batSig);
        double sig = RFXComOregonTools.getSignalStatus(batSig);
        
        boolean found = false;
        
        LOG.debug("Address: {}, Channel: {}, Temp: {}, Battery: {}, Signal: {}", deviceId, remoteChannel, temp, bat,sig);
        
        for(Device device:this.getDriver().getRunningDevices()){
            if (device.getAddress().equals(deviceId)){
                found = true;
                RFXComOregonCommand command = new RFXComOregonCommand();
                command.setTemperature(temp);
                command.setBattery(bat);
                command.setSignal(sig);
                ((RFXComDevice)device).handleData(command);
            }
        }
        if(!found && DiscoveredItemsCollection.discoveryEnabled(this.getDriver())){
            
            DiscoveredDevice newDevice = new DiscoveredDevice(deviceId,new StringBuilder(subType.getDescription()).append(" or alike").toString());
            
            newDevice.addVisualInformation("Channel",remoteChannel);
            newDevice.addVisualInformation("Supplied temp",temp);
            newDevice.addVisualInformation("Provides", "Temperature");
            newDevice.addVisualInformation("packettype", RFXComDefinitions.PacketType.OREGON50.toString());
            newDevice.addVisualInformation("packetsubtype", subType.toString());
            
            newDevice.addParameterValue("packettype", RFXComDefinitions.PacketType.OREGON50.toString());
            newDevice.addParameterValue("packetsubtype", subType.toString());
            
            newDevice.setDeviceDriver("org.pidome.driver.device.rfxcom.rFXComOregon50Device");
            
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