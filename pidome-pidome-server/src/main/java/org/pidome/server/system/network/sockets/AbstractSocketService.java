/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.sockets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.pidome.server.connector.tools.properties.IntegerPropertyBindingBean;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.connector.tools.properties.StringPropertyBindingBean;
import org.pidome.server.services.ServiceInterface;

/**
 * Base socket information class.
 * @author John
 */
public abstract class AbstractSocketService implements ServiceInterface {
    
    /**
     * The Socket port.
     * This is a bindable property
     */
    private IntegerPropertyBindingBean portNumber             = new IntegerPropertyBindingBean(0);
    
    /**
     * The Ip address for binding this service.
     * This is a bindable property.
     */
    private ObjectPropertyBindingBean<InetAddress> ipAddress  = new ObjectPropertyBindingBean<>();
    
    /**
     * The hostname.
     * This is a bindable property.
     */
    private StringPropertyBindingBean hostName                = new StringPropertyBindingBean("");
    
    /**
     * Returns the combined ip and port data as a string.
     * @return 
     */
    public final String getCombinedAddress(){
        try {
            return ipAddress.getValue().getHostAddress() +":"+portNumber.getValue();
        } catch (NullPointerException ex){
            //// Not active
            return "";
        }
    }
    
    /**
     * Sets an ip address from a string.
     * @param ipAddress 
     */
    protected void setIpAddress(String ipAddress) throws UnknownHostException{
        setIpAddress(InetAddress.getByName(ipAddress));
    }
    
    /**
     * Sets the ip address from an InetAddress
     * @param address 
     */
    protected final void setIpAddress(InetAddress address){
        ipAddress.setValue(address);
        hostName.setValue(ipAddress.getValue().getHostAddress());
    }
    
    /**
     * returns the hostName
     * @return 
     */
    protected String getHostName(){
        return hostName.getValueSafe();
    }
    
    /**
     * Sets the port
     * @param port 
     */
    protected final void setPort(int port){
        portNumber.setValue(port);
    }
    
    /**
     * Returns a bindable ip address
     * @return The ip address.
     */
    public final ObjectPropertyBindingBean<InetAddress> getIpAddress(){
        return ipAddress;
    }
    
    /**
     * Returns a bindable oprt number
     * @return The port number.
     */
    public final IntegerPropertyBindingBean getPort(){
        return portNumber;
    }
    
}