/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import org.pidome.client.phone.services.SystemService.BindExposer;

/**
 *
 * @author John
 */
public interface SystemServiceConnectorListener {
    
    public void handleSystemServiceConnected(BindExposer binder);
    
    public void handleSystemServiceDisConnected();
    
}
