/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolBase;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;

/**
 *
 * @author John
 */
public class RFXComProtoDummyHandler  extends PacketProtocolBase {

    /**
     * Constructor for received data.
     * @param driver
     * @param parser 
     */
    public RFXComProtoDummyHandler(NativeRFXComDriver driver, RFXComBasicPacketParser parser) {
        super(driver, parser);
    }
    
    /**
     * Constructor for sending data.
     * @param driver 
     * @param device 
     * @param request 
     */
    public RFXComProtoDummyHandler(NativeRFXComDriver driver, RFXComDevice device, DeviceCommandRequest request) {
        super(driver, device, request);
    }

    /**
     * Handle received data created by the received constructor.
     * @throws PacketProtocolException 
     */
    @Override
    public void handleReceived() throws PacketProtocolException {
        /// Do nothing.
    }

    /**
     * Return data to be send created by the send constructor.
     * @return
     * @throws PacketProtocolException 
     */
    @Override
    public byte[] getSend() throws PacketProtocolException {
        throw new PacketProtocolException("I do not send anything, just a dummy");
    }
    
}
