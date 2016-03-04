/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.device.rfxcom.rFXComLighting1Device.RFXComLighting1Device;
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
 *
 * @author John
 */
public class RFXComLighting1Handler extends PacketProtocolBase {
    
    Logger LOG = LogManager.getLogger(RFXComLighting1Handler.class);

    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * AC protocol.
         */
        X10(0, "X10"),
        /**
         * HA_EU
         */
        ARC(1, "Adress wheels: KlikAanKlikUit, Anslut, Chacon, CoCo, DI.O, HomeEasy (UK), Intertechno, NEXA"),
        /**
         * Elro AB400D
         */
        ELRO_AB400D(2, "Flamingo, AB400, Impuls, Sartano, Brennenstuhl"),
        /**
         * Waveman
         */
        WAVEMAN(3, "Waveman"),
        /**
         * EMW200
         */
        EMW200(4, "EMW 200"),
        /**
         * Impuls
         */
        IMPULS(5, "Impuls"),
        /**
         * RisingSun
         */
        RISING_SUN(6, "Rising Sun"),
        /**
         * Philips SBC
         */
        PHILIPS_SBC(7, "Philips SBC"),
        /**
         * EnerGenie
         */
        ENERGENIE(8, "Energenie"),
        /**
         * Energenie5
         */
        ENERGENIE5(9, "EnerGenie5"),
        /**
         * COCO GDR2
         */
        COCO_GDR2(10, "CoCo GDR2");
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
                put(PacketSubType.X10.getValue(), PacketSubType.X10);
                put(PacketSubType.ARC.getValue(), PacketSubType.ARC);
                put(PacketSubType.ELRO_AB400D.getValue(), PacketSubType.ELRO_AB400D);
                put(PacketSubType.WAVEMAN.getValue(), PacketSubType.WAVEMAN);
                put(PacketSubType.EMW200.getValue(), PacketSubType.EMW200);
                put(PacketSubType.IMPULS.getValue(), PacketSubType.IMPULS);
                put(PacketSubType.RISING_SUN.getValue(), PacketSubType.RISING_SUN);
                put(PacketSubType.PHILIPS_SBC.getValue(), PacketSubType.PHILIPS_SBC);
                put(PacketSubType.ENERGENIE.getValue(), PacketSubType.ENERGENIE);
                put(PacketSubType.ENERGENIE5.getValue(), PacketSubType.ENERGENIE5);
                put(PacketSubType.COCO_GDR2.getValue(), PacketSubType.COCO_GDR2);
            }});
    
    /**
     * Returns a subpacket type indexed by the byte's int value.
     * @return 
     */
    public static Map<Integer,RFXComLighting1Handler.PacketSubType> getSubPacketTypes(){
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
    
    public RFXComLighting1Handler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
        super(driver, parser);
    }
    
    public RFXComLighting1Handler(NativeRFXComDriver driver,RFXComDevice device, DeviceCommandRequest request){
        super(driver, device, request);
    }

    @Override
    public void handleReceived() throws PacketProtocolException {
        handleReceived(getSubPacketTypes().get(getParser().getSubType()),this.getParser().getMessageBody());
    }

    /**
     * Handles received protocol.
     */
    private void handleReceived(RFXComLighting1Handler.PacketSubType subType, byte[] bytes) throws PacketProtocolException {
        String address = new StringBuilder(RFXComBasicPacketParser.decodeSingleByte(bytes[1])).append(":").append(RFXComBasicPacketParser.decodeSingleByte(bytes[2])).toString();
        boolean found = false;
        for(Device device:this.getDriver().getRunningDevices()){
            if(device.getAddress().equals(address)){
                found = true;
                sendToDeviceByPassFilter(device, bytes);
            }
        }
        if(!found && DiscoveredItemsCollection.discoveryEnabled(this.getDriver())){
            
            DiscoveredDevice newDevice = new DiscoveredDevice(address,new StringBuilder(subType.getDescription()).append(" or compatible").toString());
            
            newDevice.addVisualInformation("Remote address",address);
            newDevice.addVisualInformation("Provides", "On/Off,Dimming,etc.. depending on the device discovered");
            newDevice.addVisualInformation("packettype", RFXComDefinitions.PacketType.LIGHTING1.toString());
            newDevice.addVisualInformation("packetsubtype", subType.toString());
            
            newDevice.addParameterValue("packettype", RFXComDefinitions.PacketType.LIGHTING1.toString());
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
        RFXComLighting1Command command = new RFXComLighting1Command();
        String commandType = RFXComBasicPacketParser.decodeSingleByte(body[3]);
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
                command.setDim(true);
            break;
            case "03":
                command.setHasBright(true);
                command.setBright(false);
            break;
            case "05":
                command.setHasGroupSwitch(true);
                command.setGroupSwitch(false);
            break;
            case "06":
                command.setHasGroupSwitch(true);
                command.setGroupSwitch(true);
            break;
            case "07":
                command.setHasChime(true);
                command.setChime(true);
            break;
        }
        ((RFXComDevice)device).handleData(command);
    }
    
    @Override
    public byte[] getSend() throws PacketProtocolException {
        return createSend(((RFXComLighting1Device)this.getDevice()).getPacketSubType(),(RFXComLighting1Device)this.getDevice(), this.getCommand());
    }
    
    /**
     * Creates the send package;
     * @param device
     * @return 
     */
    private byte[] createSend(RFXComLighting1Handler.PacketSubType type, RFXComLighting1Device device, DeviceCommandRequest request) throws PacketProtocolException {
        
        byte command= (byte)0x00;
        
        String[] id_location = device.getAddress().split(":");
        byte houseCode = translateHouseCode(id_location[0]);
        byte unitCode = (byte)Integer.parseInt(id_location[1]);
        
        switch(request.getControlId()){
            case "switch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x01;
                } else {
                    command= (byte)0x00;
                }
            break;
            case "dim":
                command = (byte)0x02;
            break;
            case "bright":
                command= (byte)0x03;
            break;
            case "groupswitch":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x04;
                } else {
                    command= (byte)0x05;
                }
            break;
            case "chime":
                if((boolean)request.getCommandValue()==true){
                    command = (byte)0x07;
                    unitCode= (byte)0x08;
                }
            break;
        }
        return new byte[] { (byte)0x07, (byte)0x10, (byte)type.getValue(), (byte)0x00, houseCode, unitCode, command, (byte)0x70 };
    }
    
    private byte translateHouseCode(String supplied){
        switch(supplied){
            case "A":
                return (byte)0x41;
            case "B":
                return (byte)0x42;
            case "C":
                return (byte)0x43;
            case "D":
                return (byte)0x44;
            case "E":
                return (byte)0x45;
            case "F":
                return (byte)0x46;
            case "G":
                return (byte)0x47;
            case "H":
                return (byte)0x48;
            case "I":
                return (byte)0x49;
            case "J":
                return (byte)0x4A;
            case "K":
                return (byte)0x4B;
            case "L":
                return (byte)0x4C;
            case "M":
                return (byte)0x4D;
            case "N":
                return (byte)0x4E;
            case "O":
                return (byte)0x4F;
            case "P":
                return (byte)0x50;
            default:
                return (byte)0x41;
        }
    }
    
}