/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver;

import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions.PacketType;

/**
 * Creates a basic packet structure.
 * @author John
 */
public class RFXComBasicPacketParser {
    
    private Logger LOG = LogManager.getLogger(RFXComBasicPacketParser.class);
    
    /**
     * (B/b)ytes collection
     */
    byte[] bytes;
    
    /**
     * Package full length without first byte.
     */
    int length;
    
    /**
     * The current package sequence reported.
     */
    int sequence;
    
    /**
     * The packet type.
     */
    PacketType packetType;
    
    /**
     * The subtype int reference.
     */
    int subType;
    
    /**
     * Constructor.
     * @param bytes 
     */
    protected RFXComBasicPacketParser(byte[] bytes){
        this.bytes = bytes;
    }
    
    /**
     * Decodes a package.
     * @throws org.pidome.driver.driver.nativeRFXComDriver.RFXComBasicPacketParserException
     */
    protected final void decode() throws RFXComBasicPacketParserException {
        try {
            if (bytes.length-1 == (bytes[0] & 0xFF)){
                try {
                    LOG.debug("Decoding for type: {}, sub: {}, sequence: {}",(bytes[1] & 0xFF),(bytes[2] & 0xFF),(bytes[3] & 0xFF));
                    packetType = RFXComDefinitions.getPacketTypes().get((bytes[1] & 0xFF));
                    subType    = ((bytes[2] & 0xFF));
                    sequence   = (bytes[3] & 0xFF);
                } catch (NullPointerException ex){
                    throw new RFXComBasicPacketParserException("Unsupported protocol, not implemented or unknown", ex);
                }
            } else {
                throw new RFXComBasicPacketParserException("Received data differs from reported length. Reported: "+(bytes[0] & 0xFF)+", real: " + bytes.length);
            }
        } catch (IndexOutOfBoundsException ex){
            throw new RFXComBasicPacketParserException("Invalid package", ex);
        }
    }
    
    /**
     * Returns the subtype as int.
     * @return 
     */
    public final int getSubType(){
        return subType;
    }
    
    /**
     * Returns the bytes excluding the length byte.
     * This in fact returns a copy of the original bytes (from byte[1] to byte[length-1]).
     * @return 
     */
    public final byte[] getBytes(){
        byte[] newArray = new byte[bytes.length-1];
        System.arraycopy( bytes, 1, newArray, 0, bytes.length-1 );
        return newArray;
    }
    
    /**
     * Returns the bytes including the length byte.
     * @return 
     */
    public final byte[] getAbsoluteBytes(){
        return this.bytes;
    }
    
    /**
     * This returns the message body.
     * The body is the byte array minus the packet type, sub packet type and sequence number.
     * @return 
     */
    public final byte[] getMessageBody(){
        byte[] newArray = new byte[bytes.length-4];
        System.arraycopy( bytes, 4, newArray, 0, bytes.length-4);
        char[] hexChars = new char[newArray.length * 2];
        for (int j = 0; j < newArray.length; j++) {
            int v = newArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String string = new String(hexChars);
        LOG.trace("Message body: {}", string);
        return newArray;
    }
    
    /**
     * Returns the package length excluding the length byte.
     * @return 
     */
    public final int getLength(){
        return bytes.length-1;
    }
    
    /**
     * Returns the byte length including the length byte.
     * @return 
     */
    public final int getAbsoluteLength(){
        return bytes.length;
    }
    
    /**
     * Returns the packet type.
     * @return 
     */
    public final PacketType getPacketType(){
        return this.packetType;
    }
    
    /**
     * Returns the packet byte as hex in String format.
     * @return 
     */
    public String getPacketTypeByteChar(){
        return decodeSingleByte(bytes[1]);
    }
    
    /**
     * Returns the packet byte as hex in String format.
     * @return 
     */
    public String getPacketSubTypeByteChar(){
        return decodeSingleByte(bytes[2]);
    }
    
    /**
     * Returns the current sequence.
     * @return 
     */
    public int getSequence(){
        return sequence;
    }
    
    /**
     * Array of possible hex chars.
     */
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Quick to hex parser for debug output.
     * @return 
     */
    @Override
    public String toString() {
        return decodeAllBytes(bytes);
    }
    
    /**
     * Decode's all bytes.
     * @param toDecode
     * @return 
     */
    public static String decodeAllBytes(byte[] toDecode){
        char[] hexChars = new char[toDecode.length * 2];
        for (int j = 0; j < toDecode.length; j++) {
            int v = toDecode[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * Decodes a single byte to return.
     * @param toDecode
     * @return 
     */
    public static String decodeSingleByte(byte toDecode){
        char[] hexChar = new char[2];
        int value = (toDecode & 0xFF);
        hexChar[0] = hexArray[value >>> 4];
        hexChar[1] = hexArray[value & 0x0F];
        return new String(hexChar);
    }
    
}
