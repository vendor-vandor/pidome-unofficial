/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.peripherals.pidomeNativeSerial;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.peripherals.pidomeNativeUSBSerial.PidomeNativeUSBSerial;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;


/**
 *
 * @author John Sirach
 */
public class PidomeNativeSerial extends PidomeNativeUSBSerial {

    static Logger LOG = LogManager.getLogger(PidomeNativeSerial.class);
    
    public PidomeNativeSerial() throws PeripheralHardwareException {
        super();
    }
    
}