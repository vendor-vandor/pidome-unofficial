/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos;

import org.pidome.driver.device.rfxcom.RFXComDevice;

/**
 * Base RFXCom command.
 * @author John
 */
public abstract class RFXComCommand {
    
    public abstract void handle(RFXComDevice device);
    
}
