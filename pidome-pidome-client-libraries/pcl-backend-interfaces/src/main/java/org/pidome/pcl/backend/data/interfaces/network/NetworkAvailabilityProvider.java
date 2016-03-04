/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author John
 */
public interface NetworkAvailabilityProvider {
    
    /**
     * Network status.
     */
    public enum Status {
        /**
         * There is a compatible network connection available.
         */
        NETWORKAVAILABLE,
        /**
         * No compatible network connection available.
         */
        NETWORKUNAVAILABLE;
    }
 
    /**
     * For platforms supporting auto discovery of network interfaces.
     * @throws UnknownHostException When discover delivers no result.
     */
    public abstract void discover() throws UnknownHostException;
    
    /**
     * Get the current ip address.
     * @return the current ip address.
     * @throws UnknownHostException When no ip is known.
     */
    public abstract InetAddress getIpAddress() throws UnknownHostException;
    
    /**
     * Returns the current subnet.
     * @return The current subnetmask.
     * @throws UnknownHostException When no subnet is known.
     */
    public abstract InetAddress getSubnetAddress() throws UnknownHostException;
    
    /**
     * Returns the broadcast address.
     * @return The current broadcast address.
     * @throws UnknownHostException if no broadcast address is known.
     */
    public abstract InetAddress getBroadcastAddress() throws UnknownHostException;
            
    /**
     * Adds a network availability listener.
     * @param l Listener.
     */
    public abstract void addEventListener(NetworkAvailabilityEventListener l);

    /**
     * Removes a network availability listener.
     * @param l The listener.
     */
    public abstract void removeEventListener(NetworkAvailabilityEventListener l);
    
}