/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEventListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;

/**
 *
 * @author John
 */
public class IOSNetworkAvailabilityProvider implements NetworkAvailabilityProvider{

    public IOSNetworkAvailabilityProvider(){
        
    }
    
    @Override
    public void discover() throws UnknownHostException {
        /// function which starts the discovery
    }

    @Override
    public InetAddress getIpAddress() throws UnknownHostException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InetAddress getSubnetAddress() throws UnknownHostException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InetAddress getBroadcastAddress() throws UnknownHostException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEventListener(NetworkAvailabilityEventListener nl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEventListener(NetworkAvailabilityEventListener nl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
