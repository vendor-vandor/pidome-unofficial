/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.network;

/**
 *
 * @author John
 */
public interface NetworkAvailabilityEventListener {
    public void handleNetworkAvailabilityEvent(NetworkAvailabilityEvent event);
}
