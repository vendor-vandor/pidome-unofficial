/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.driver.nativeRFXComDriver.NativeRFXComDriver;
import org.pidome.driver.driver.nativeRFXComDriver.RFXComBasicPacketParser;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;

/**
 * Minimal available functions per protocol.
 * @author John
 */
public abstract class PacketProtocolBase {
    
    /**
     * The parser.
     * Only available when there is data received from the hardware.
     */
    private RFXComBasicPacketParser parser;
    /**
     * The driver.
     */
    private NativeRFXComDriver driver;
    /**
     * The device wanting to send.
     * Only available when a device wants to send.
     */
    private RFXComDevice device;
    
    /**
     * The command issued by the device.
     */
    private DeviceCommandRequest request;
    
    /**
     * Used When parsing.
     * @param driver
     * @param parser 
     */
    public PacketProtocolBase(NativeRFXComDriver driver,RFXComBasicPacketParser parser){
        this.driver = driver;
        this.parser = parser;
    }
    
    /**
     * USed when command is received.
     * @param driver
     * @param device
     * @param request 
     */
    public PacketProtocolBase(NativeRFXComDriver driver,RFXComDevice device, DeviceCommandRequest request){
        this.driver  = driver;
        this.device  = device;
        this.request = request;
    }
    
    /**
     * Returns the driver.
     * @return 
     */
    protected final NativeRFXComDriver getDriver(){
        return this.driver;
    }
    
    /**
     * Returns the device.
     * @return 
     */
    protected final RFXComDevice getDevice(){
        return this.device;
    }
    
    /**
     * Returns the request issued by the device.
     * Only available when a device makes a request.
     * @return 
     */
    protected final DeviceCommandRequest getCommand(){
        return this.request;
    }
    
    /**
     * Returns the parser;
     * @return 
     */
    protected final RFXComBasicPacketParser getParser(){
        return this.parser;
    }
    
    /**
     * Handles data received.
     * @throws PacketProtocolException 
     */
    public abstract void handleReceived() throws PacketProtocolException;
 
    /**
     * Creates bytes to be send.
     * @return The bytes to be send.
     * @throws PacketProtocolException 
     */
    public abstract byte[] getSend() throws PacketProtocolException;
    
}
