/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import org.pidome.client.phone.dialogs.settings.LocalizationInfoInterface;

/**
 *
 * @author John
 */
public interface ServiceConnector {
    
    public void startService();
    
    public void stopService();
    
    public void setServiceConnectionListener(ServiceConnectorListener listener);
    
    public LifeCycleHandlerInterface getLifeCycleHandler();
    
    public LocalizationInfoInterface getLocalizationService() throws UnsupportedOperationException;
    
}
