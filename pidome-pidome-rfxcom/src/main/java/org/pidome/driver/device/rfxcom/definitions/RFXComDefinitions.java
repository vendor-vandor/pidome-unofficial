/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.definitions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Constants used.
 * @author John
 */
public class RFXComDefinitions {
    
    /**
     * Supported protocols by this plugin.
     */
    public enum PacketType {
        /**
         * RFXCom Control message type.
         */
        RFXCOM_CTRL(0),
        /**
         * RFXCom message type.
         */
        RFXCOM_IFACE(1),
        /**
         * RFXCom transmit result.
         */
        RFXCOM_TRANS_MSG(2),
        /**
         * Lighting 1 group
         */
        LIGHTING1(16),
        /**
         * Lighting 2 group.
         */
        LIGHTING2(17),
        /**
         * Lighting 5 group
         */
        LIGHTING5(20),
        /**
         * Oregon scientific.
         */
        OREGON50(80),
        OREGON51(81),
        OREGON52(82),
        OREGON54(84),
        OREGON55(85),
        OREGON56(86);
        
        /**
         * Supplying value.
         */
        private final int value;

        /**
         * Enum constructor for supplying a value to the enum.
         * @param newValue 
         */
        private PacketType(final int value) {
            this.value = value;
        }

        /**
         * Returns the enum value;
         * @return 
         */
        public int getValue() { return value; }
        
    }   
    
    /**
     * Supported sub packages by this plugin.
     */
    public enum PacketSubType {
        /**
         * Send ok.
         */
        ACK(0),
        /**
         * Send with delay.
         */
        ACK_DELAY(1),
        /**
         * Not send.
         */
        NAK(2),
        /**
         * Not send, invalid.
         */
        NAK_INVALID(3);
        /////Lighting2
        /**
         * AC protocol.
         */
        
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
    private final static Map<Integer,PacketType> packetTypes = Collections.unmodifiableMap(new HashMap<Integer,PacketType>(){{
                put(PacketType.LIGHTING1.getValue(),        PacketType.LIGHTING1);
                put(PacketType.LIGHTING2.getValue(),        PacketType.LIGHTING2);
                put(PacketType.LIGHTING5.getValue(),        PacketType.LIGHTING5);
                put(PacketType.OREGON50.getValue(),         PacketType.OREGON50);
                put(PacketType.OREGON51.getValue(),         PacketType.OREGON51);
                put(PacketType.OREGON52.getValue(),         PacketType.OREGON52);
                put(PacketType.OREGON54.getValue(),         PacketType.OREGON54);
                put(PacketType.RFXCOM_TRANS_MSG.getValue(), PacketType.RFXCOM_TRANS_MSG);
                put(PacketType.RFXCOM_CTRL.getValue(),      PacketType.RFXCOM_CTRL);
                put(PacketType.RFXCOM_IFACE.getValue(),     PacketType.RFXCOM_IFACE);
            }});
    
    /**
     * Returns a map containing supported base protocols mapped by byte (int value).
     * @return 
     */
    public static Map<Integer,PacketType> getPacketTypes(){
        return packetTypes;
    }
    
}
