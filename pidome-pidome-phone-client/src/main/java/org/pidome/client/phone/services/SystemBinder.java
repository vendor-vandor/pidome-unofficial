/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import org.pidome.client.phone.dialogs.settings.LocalizationInfoInterface;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public interface SystemBinder {
    
    public PCCSystem getPCCSystem();

    public void setSignalHandler(ServiceConnectorListener receiver);
    
    public LifeCycleHandlerInterface getLifeCycleHandler();
    
    public LocalizationInfoInterface getPresenceServiceSettings();
    
}