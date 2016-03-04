/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.client;

import org.pidome.client.services.aidl.service.SystemServiceAidlInterface;

/**
 *
 * @author John
 */
public interface SystemServiceConnectorListenerAidl {
    
    public void handleSystemServiceConnected(SystemServiceAidlInterface service);
    
    public void handleSystemServiceDisConnected();
    
}
