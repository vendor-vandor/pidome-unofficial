/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.tools.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.connector.tools.properties.ReadOnlyObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class Networking {
    static ObjectPropertyBindingBean<InetAddress> ipAddress = new ObjectPropertyBindingBean<>();
    
    /**
     * Returns the found ip address.
     * @return
     * @throws UnknownHostException 
     */
    public final static ReadOnlyObjectPropertyBindingBean<InetAddress> getIpAddressProperty() throws UnknownHostException {
        if (ipAddress.get() == null) {
            throw new UnknownHostException("No ip address found in autodiscovery, set static data in system config");
        } else {
            return ipAddress.getReadOnlyBooleanPropertyBindingBean();
        }
    }
    
    public static void setCurrentIpAddress(InetAddress address){
        ipAddress.set(address);
    }
    
}
