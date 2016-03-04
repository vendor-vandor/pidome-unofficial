/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public interface ServiceConnectorListener {
    
    public void handleUserLoggedIn();

    public void handleUserLoggedOut();
    
    public void serviceConnected(PCCSystem system);
    
    public void serviceDisconnectedConnected();
    
}