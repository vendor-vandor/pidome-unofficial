/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.device.rfxcom.rFXComLighting2Device.RFXComLighting2Device;
import org.pidome.driver.driver.nativeRFXComDriver.NativeRFXComDriver;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolBase;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.RFXComBasicPacketParser;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;

/**
 * Parser for lighting2 devices.
 * @author John
 */
public final class RFXComLighting2Handler extends PacketProtocolBase {
    
    Logger LOG = LogManager.getLogger(RFXComLighting2Handler.class);
    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * AC protocol.
         */
        AC(0, "Learning: KlikAanKlikUit, Anslut, Chacon, CoCo, DI.O, HomeEasy (UK), Intertechno, NEXA"),
        /**
         * HA_EU
         */
        HE_EU(1, "HomeEasy EU"),
        /**
         * Anslut.
         */
        ANSLUT(2, "Anslut"),
        /**
         * Kambrook
         */
        KAMBROOK(3, "Kambrook");
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
                put(PacketSubType.AC.getValue(), PacketSubType.AC);
                put(PacketSubType.HE_EU.getValue(), PacketSubType.HE_EU);
                put(PacketSubType.ANSLUT.getValue(), PacketSubType.ANSLUT);
                put(PacketSubType.KAMBROOK.getValue(), PacketSubType.KAMBROOK);
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
    public RFXComLighting2Handler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
        super(driver, parser);
    }

    /**
     * Constructor.
     * @param driver 
     * @param device 
     * @param request 
     */
    public RFXComLighting2Handler(NativeRFXComDriver driver, RFXComDevice device, DeviceCommandRequest request) {
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
     * Handles received AC protocol.
     */
    private void handleReceived(PacketSubType subType, byte[] bytes) throws PacketProtocolException {
        String[] deviceId = new String[4];
        for(int i=0;i<4;i++){
            deviceId[i] = RFXComBasicPacketParser.decodeSingleByte(bytes[i]);
        }
        String baseAddress = RFXComBasicPacketParser.decodeSingleByte(bytes[4]);
        String address = new StringBuilder(String.join(",", deviceId)).append(":").append(baseAddress).toString();
        boolean found = false;
        for(Device device:this.getDriver().getRunningDevices()){
            if(device.getAddress().equals(address)){
                found = true;
                sendToDeviceByPassFilter(device, bytes);
            }
        }
        if(!found && DiscoveredItemsCollection.discoveryEnabled(this.getDriver())){
            
            DiscoveredDevice newDevice = new DiscoveredDevice(address,new StringBuilder(subType.getDescription()).append(" or alike").toString());
            
            newDevice.addVisualInformation("Remote address",baseAddress);
            newDevice.addVisualInformation("Provides", "On/Off,Dimming,etc.. depending on the device discovered");
            newDevice.addVisualInformation("packettype", RFXComDefinitions.PacketType.LIGHTING2.toString());
            newDevice.addVisualInformation("packetsubtype", subType.toString());
            
            newDevice.addParameterValue("packettype", RFXComDefinitions.PacketType.LIGHTING2.toString());
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
    private void sendToDeviceByPassFilter(Device device, byte[] body){
        RFXComLighting2Command command = new RFXComLighting2Command();
        String commandType = RFXComBasicPacketParser.decodeSingleByte(body[5]);
        LOG.debug("Setting data for command type: {}", commandType);
        switch(commandType){
            case "00":
                command.setHasSwitch(true);
                command.setSwitch(false);
            break;
            case "01":
                command.setHasSwitch(true);
                command.setSwitch(true);
            break;
            case "02":
                command.setHasDim(true);
                command.setDim(Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(body[6])));
            break;
            case "03":
                command.setHasGroupSwitch(true);
                command.setGroupSwitch(false);
            break;
            case "04":
                command.setHasGroupSwitch(true);
                command.setGroupSwitch(true);
            break;
            case "05":
                command.setHasGroupDim(true);
                command.setGroupDim(Integer.parseInt(RFXComBasicPacketParser.decodeSingleByte(body[6])));
            break;
        }
        ((RFXComDevice)device).handleData(command);
    }
    
    /**
     * Creates the bytes to be send.
     * @return
     * @throws PacketProtocolException 
     */
    @Override
    public byte[] getSend() throws PacketProtocolException {
        return createSend(((RFXComLighting2Device)this.getDevice()).getPacketSubType(),(RFXComLighting2Device)this.getDevice(), this.getCommand());
    }
    
    /**
     * Creates the send package;
     * @param device
     * @return 
     */
    private byte[] createSend(PacketSubType type, RFXComLighting2Device device, DeviceCommandRequest request) throws PacketProtocolException {
        byte level = (byte)0x0F;
        byte command= (byte)0x00;
        switch(request.getControlId()){
            case "switch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x01;
                } else {
                    command= (byte)0x00;
                }
            break;
            case "groupswitch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x04;
                } else {
                    command= (byte)0x03;
                }
            break;
            case "dimlevel":
                command = (byte)0x02;
                level = createDimLevel(Integer.parseInt(request.getCommandValue().toString()));
            break;
            case "groupdimlevel":
                command = (byte)0x05;
                level = createDimLevel(Integer.parseInt(request.getCommandValue().toString()));
            break;
        }
        String[] id_location = device.getAddress().split(":");
        byte unitCode = (byte)Integer.parseInt(id_location[1],16);
        String[] id = id_location[0].split(",");
        byte[] idSet = new byte[4];
        for(int i = 0; i < id.length; i++){
            idSet[i] = (byte)Integer.parseInt(id[i], 16);
        }
        return new byte[] { (byte)0x0B, (byte)0x11, (byte)type.getValue(), (byte)0x01, idSet[0], idSet[1], idSet[2], idSet[3], unitCode, command, level, (byte)0x00 };
    }
 
    /**
     * Creates the dim level (0-100 -> 0 - 15)
     * @param value
     * @return
     * @throws PacketProtocolException 
     */
    private byte createDimLevel(int value) throws PacketProtocolException {
        if(value>100 || value<0){
            throw new PacketProtocolException("Impossible dim value: " + value);
        } else {
            int level = value/6;
            switch(level){
                case 0:
                    return (byte)0x00;
                case 1:
                    return (byte)0x01;
                case 2:
                    return (byte)0x02;
                case 3:
                    return (byte)0x03;
                case 4:
                    return (byte)0x04;
                case 5:
                    return (byte)0x05;
                case 6:
                    return (byte)0x06;
                case 7:
                    return (byte)0x07;
                case 8:
                    return (byte)0x08;
                case 9:
                    return (byte)0x09;
                case 10:
                    return (byte)0x0A;
                case 11:
                    return (byte)0x0B;
                case 12:
                    return (byte)0x0C;
                case 13:
                    return (byte)0x0D;
                case 14:
                    return (byte)0x0E;
                default:
                    return (byte)0x0F;
            }
        }
    }
    
}