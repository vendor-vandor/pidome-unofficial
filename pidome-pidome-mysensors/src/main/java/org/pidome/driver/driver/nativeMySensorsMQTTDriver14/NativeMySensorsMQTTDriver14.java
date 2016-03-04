/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.driver.nativeMySensorsMQTTDriver14;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.emulators.NativePiDomePluginProxyDriverEmulator;
import org.pidome.server.connector.emulators.PluginPeripheral;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;

/**
 *
 * @author John
 */
public class NativeMySensorsMQTTDriver14 extends NativePiDomePluginProxyDriverEmulator implements WebPresentAddExistingDeviceInterface {

    static Logger LOG = LogManager.getLogger(NativeMySensorsMQTTDriver14.class);
    
    @Override
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException {
        try {
            ((WebPresentAddExistingDeviceInterface)((PluginPeripheral)this.getHardwareDriver()).getPluginLink()).handleNewDeviceRequest(request);
        } catch (Exception ex){
            LOG.error("New device request done but not implemented correctly: {}", ex.getMessage());
            throw new PeripheralDriverDeviceMutationException(ex.getMessage());
        }
    }

    @Override
    public void discoveryEnabled() {
        ///Not used
    }

    @Override
    public void discoveryDisabled() {
        ///Not used
    }
    
}