/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting5;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions.PacketType;
import org.pidome.driver.device.rfxcom.rFXComLighting5Device.RFXComLighting5Device;
import org.pidome.driver.driver.nativeRFXComDriver.NativeRFXComDriver;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolBase;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.RFXComBasicPacketParser;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.tools.MathImpl;

/**
 * Parser for lighting2 devices.
 * @author John
 */
public final class RFXComLighting5Handler extends PacketProtocolBase {
    
    Logger LOG = LogManager.getLogger(RFXComLighting5Handler.class);
    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * Siemens lightwave.
         */
        LIGHTWAVERF(0);
        /**
         * Supplying value.
         */
        private final int value;

        /**
         * Enum constructor for supplying a value to the enum.
         * @param newValue 
         */
        private PacketSubType(final int value) {
            this.value = value;
        }

        /**
         * Returns the enum value;
         * @return 
         */
        public int getValue() { return value; }
        
    }
    
    /**
     * Base protocol mapper.
     */
    private final static Map<Integer,PacketSubType> subPacketTypes = Collections.unmodifiableMap(new HashMap<Integer,PacketSubType>(){{
                put(PacketSubType.LIGHTWAVERF.getValue(), PacketSubType.LIGHTWAVERF);
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
    public RFXComLighting5Handler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
        super(driver, parser);
    }

    /**
     * Constructor.
     * @param driver 
     * @param device 
     * @param request 
     */
    public RFXComLighting5Handler(NativeRFXComDriver driver, RFXComDevice device, DeviceCommandRequest request) {
        super(driver, device, request);
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
     * Handles received protocol.
     */
    private void handleReceived(PacketSubType subType, byte[] bytes) throws PacketProtocolException {
        String[] deviceId = new String[3];
        for(int i=0;i<3;i++){
            deviceId[i] = RFXComBasicPacketParser.decodeSingleByte(bytes[i]);
        }
        String baseAddress = RFXComBasicPacketParser.decodeSingleByte(bytes[3]);
        String address = new StringBuilder(String.join(",", deviceId)).append(":").append(baseAddress).toString();
        boolean found = false;
        for(Device device:this.getDriver().getRunningDevices()){
            if(device.getAddress().equals(address)){
                found = true;
                sendToDeviceByPassFilter(subType, device, bytes);
            }
        }
        if(!found && DiscoveredItemsCollection.discoveryEnabled(this.getDriver())){
            
            DiscoveredDevice newDevice = new DiscoveredDevice(address,"LightwaveRF");
            
            newDevice.addVisualInformation("Possible types", "LightwaveRF");
            newDevice.addVisualInformation("Remote address",baseAddress);
            newDevice.addVisualInformation("packettype", PacketType.LIGHTING5.toString());
            newDevice.addVisualInformation("packetsubtype", subType.toString());
            
            newDevice.addParameterValue("packettype", PacketType.LIGHTING5.toString());
            newDevice.addParameterValue("packetsubtype", subType.toString());
            
            try {
                DiscoveredItemsCollection.addDiscoveredDevice(this.getDriver(), newDevice);
            } catch (DeviceDiscoveryServiceException ex) {
                LOG.warn("Could not add device to discovery table: {}", ex.getMessage());
            }

        }
    }

    /**
     * Delivers data to the correct device.
     * This function bypasses the filer function which is going to be fased out.
     * @param deviceAddress
     * @param messageType
     * @param data 
     */
    private void sendToDeviceByPassFilter(PacketSubType subType, Device device, byte[] body){
        String commandType = RFXComBasicPacketParser.decodeSingleByte(body[4]);
        LOG.debug("Setting data for subtype: {} command: {}", subType, commandType);
        switch(subType){
            case LIGHTWAVERF:
                RFXComLighting5LightwaveRFCommand command = new RFXComLighting5LightwaveRFCommand();
                command.setCommand(commandType);
                switch(commandType){
                    case "0x10":
                        try {
                            command.setLevel(createReversedDimLevel(Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(body[5]),16)));
                        } catch (PacketProtocolException ex) {
                            LOG.error("Could not create dim level: {}", ex.getMessage());
                        }
                    break;
                }
                ((RFXComDevice)device).handleData(command);
            break;
        }
    }
    
    /**
     * Creates the bytes to be send.
     * @return
     * @throws PacketProtocolException 
     */
    @Override
    public byte[] getSend() throws PacketProtocolException {
        return createSend(((RFXComLighting5Device)this.getDevice()).getPacketSubType(),(RFXComLighting5Device)this.getDevice(), this.getCommand());
    }
    
    /**
     * Creates the send package;
     * @param device
     * @return 
     */
    private byte[] createSend(PacketSubType type, RFXComLighting5Device device, DeviceCommandRequest request) throws PacketProtocolException {
        byte level = (byte)0x00;
        byte command= (byte)0x00;
        switch(request.getControlId()){
            case "switch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x01;
                } else {
                    command= (byte)0x00;
                }
            break;
            case "groupoff":
                command= (byte)0x02;
            break;
            case "moodselect1":
                command= (byte)0x03;
            break;
            case "moodselect2":
                command= (byte)0x04;
            break;
            case "moodselect3":    
                command= (byte)0x05;
            break;
            case "moodselect4":
                command= (byte)0x06;
            break;
            case "moodselect5":
                command= (byte)0x07;
            break;
            case "lockswitch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x0B;
                } else {
                    command= (byte)0x0A;
                }
            break;
            case "alllock":
                command= (byte)0x0C;
            break;
            case "relayswitch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x0F;
                } else {
                    command= (byte)0x0D;
                }
            break;
            case "relaystop":
                command= (byte)0x0E;
            break;
            case "level":
                command = (byte)0x10;
                level = createDimLevel(Integer.parseInt(request.getCommandValue().toString()));
            break;
            case "colornext":
                command= (byte)0x11;
            break;
            case "colortone":
                command= (byte)0x12;
            break;
            case "colorcycle":
                command= (byte)0x13;
            break;
        }
        String[] id_location = device.getAddress().split(":");
        byte unitCode = (byte)Integer.parseInt(id_location[1],16);
        String[] id = id_location[0].split(",");
        byte[] idSet = new byte[3];
        for(int i = 0; i < id.length; i++){
            idSet[i] = (byte)Integer.parseInt(id[i], 16);
        }
        return new byte[] { (byte)0x0A, (byte)0x14, (byte)type.getValue(), (byte)0x01, idSet[0], idSet[1], idSet[2], unitCode, command, level, (byte)0x00 };
    }
 
    /**
     * Creates the dim level (0-100 -> 1 - 31).
     * @param value
     * @return
     * @throws PacketProtocolException 
     */
    private byte createDimLevel(int value) throws PacketProtocolException {
        if(value>100 || value<0){
            throw new PacketProtocolException("Impossible dim value: " + value);
        }
        return (byte)((int)MathImpl.map(value, 0, 100, 0, 31));
    }
    
    /**
     * Creates the reversed dim level (1 - 31 -> 0 - 100).
     * @param value
     * @return
     * @throws PacketProtocolException 
     */
    private int createReversedDimLevel(int value) throws PacketProtocolException {
        if(value<0 || value>31){
            throw new PacketProtocolException("Impossible dim value: " + value);
        }
        return ((int)MathImpl.map(value, 0, 31, 0, 100));
    }
   
}