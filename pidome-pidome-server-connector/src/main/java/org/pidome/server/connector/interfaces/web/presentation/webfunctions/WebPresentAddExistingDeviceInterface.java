/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation.webfunctions;

import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryBaseInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;

/**
 *
 * @author John
 */
public interface WebPresentAddExistingDeviceInterface extends DeviceDiscoveryBaseInterface {
    /**
     * Handle a new device addition request.
     * @param request
     * @throws PeripheralDriverDeviceMutationException 
     */
    public void handleNewDeviceRequest(WebPresentAddExistingDeviceRequest request) throws PeripheralDriverDeviceMutationException;
    
}