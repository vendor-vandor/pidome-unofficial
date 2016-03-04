/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pidome.server.system.network;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.networking.Networking;
import org.pidome.server.connector.tools.properties.BooleanPropertyBindingBean;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.connector.tools.properties.ReadOnlyBooleanPropertyBindingBean;
import org.pidome.server.connector.tools.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.server.services.provider.CertGen;
import org.pidome.server.services.provider.CertGenException;

import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

public final class Network {

    static ObjectPropertyBindingBean<InetAddress> ipAddress = new ObjectPropertyBindingBean<>();
    static ObjectPropertyBindingBean<InetAddress> broadcastAddress = new ObjectPropertyBindingBean<>();
    InetAddress ipSubnet;
    
    static BooleanPropertyBindingBean available = new BooleanPropertyBindingBean(false);
    
    static Logger LOG = LogManager.getLogger(Network.class);
    
    static List<NetworkEventListener> _listeners = new ArrayList();

    /**
     * Constructor.
     */
    public Network(){}
    
    /**
     * Starts network routines.
     * @throws java.net.UnknownHostException
     */
    public final void startRoutines() throws UnknownHostException{
        setNetInterfaceParams();
    }
    
    /**
     * Looks for the networking devices and sets the addresses.
     * When searching for addresses it used the first found interface and only ip 4 as of this moment.
     * If you use multiple ip addresses you should set the addresses you want to use for the services in the configuration file.
     * @throws UnknownHostException 
     */
    void setNetInterfaceParams() throws UnknownHostException {
        try {
            if (SystemConfig.getProperty("system", "displayclients.ip").equals("network.autodiscovery")) {
                LOG.info("Autodiscovery of network interface addresses");
                Enumeration<NetworkInterface> list;
                try {
                    list = NetworkInterface.getNetworkInterfaces();
                    while (list.hasMoreElements()) {
                        NetworkInterface iface = (NetworkInterface) list.nextElement();

                        if (iface == null) {
                            continue;
                        }

                        if (!iface.isLoopback() && iface.isUp()) {
                            Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
                            while (it.hasNext()) {
                                InterfaceAddress address = (InterfaceAddress) it.next();
                                if (address == null || address.getAddress() instanceof Inet6Address) {
                                    continue;
                                } else {
                                    ipAddress.setValue(address.getAddress());
                                    Networking.setCurrentIpAddress(ipAddress.get());
                                    LOG.debug("Found ip address" + address);
                                }
                                InetAddress broadcast = address.getBroadcast();
                                if (broadcast != null) {
                                    broadcastAddress.setValue(broadcast);
                                    LOG.debug("Found broadcast address" + broadcast);
                                }
                                InetAddress subnet = getIPv4LocalNetMask(ipAddress.get(), address.getNetworkPrefixLength());
                                if (subnet != null) {
                                    setSubnetAddress(subnet);
                                    LOG.debug("Found subnet " + subnet);
                                }
                                break;
                            }
                        }
                    }
                } catch (SocketException ex) {
                    LOG.error("Could not create socket", ex);
                    throw new UnknownHostException("Could not create socket.");
                }
            } else {
                LOG.info("Using config file for ip address settings");
                if (SystemConfig.getProperty("system", "network.ip").equals("network.autodiscovery")) {
                    LOG.error("Set ip address in system config");
                    throw new UnknownHostException("Set ip address in system config");
                } else {
                    ipAddress.setValue(InetAddress.getByName(SystemConfig.getProperty("system", "network.ip")));
                    LOG.debug("Found ip address: " + ipAddress);
                }
                if (SystemConfig.getProperty("system", "network.broadcastip").equals("network.autodiscovery")) {
                    LOG.error("Set broadcast address in system config");
                    throw new UnknownHostException("Set broadcast address in system config");
                } else {
                    broadcastAddress.setValue(InetAddress.getByName(SystemConfig.getProperty("system", "network.broadcastip")));
                    LOG.debug("Found breoadcast address: " + broadcastAddress);
                }
            }
            Map<String, Object> eventDetails = new HashMap<>();
            eventDetails.put("serviceName", "NetInterface");
            eventDetails.put("serviceIp",    getIpAddressProperty().get().toString());
            eventDetails.put("servicePort", "N/A");
            _fireNetworkEvent(NetworkEvent.AVAILABLE);
            LOG.info("Network interface loaded");
        } catch (ConfigPropertiesException e) {
            LOG.error(e.getMessage(), e);
            LOG.info("Network interface could not be loaded: {}",e.getMessage());
            throw new UnknownHostException(e.getMessage());
        }
    }

    /**
     * Creates the interface parameters.
     * @throws UnknownHostException 
     */
    public void createInterfaceParams() throws UnknownHostException {
        setNetInterfaceParams();
    }

    public final static ReadOnlyBooleanPropertyBindingBean availabilityProperty(){
        return available.getReadOnlyBooleanPropertyBindingBean();
    }
    
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
    
    /**
     * Returns the subnet address.
     * @return
     * @throws UnknownHostException 
     */
    public final InetAddress getSubnetAddress() throws UnknownHostException {
        if (ipSubnet == null) {
            throw new UnknownHostException("No subnet address found in autodiscovery, set static data in system config");
        } else {
            return ipSubnet;
        }
    }

    /**
     * Returns the broadcast addresss.
     * @return
     * @throws UnknownHostException 
     */
    public final static ReadOnlyObjectPropertyBindingBean<InetAddress> getBroadcastAddressProperty() throws UnknownHostException {
        if (broadcastAddress == null) {
            throw new UnknownHostException("No broadcast address found in autodiscovery, set static data in system config");
        } else {
            return broadcastAddress.getReadOnlyBooleanPropertyBindingBean();
        }
    }

    /**
     * Get network mask for the IP address and network prefix specified.
     * The network mask will be returned has an IP, thus you can
     * print it out with .getHostAddress()...
     */
    static InetAddress getIPv4LocalNetMask(InetAddress ip, int netPrefix) {

        try {
            // Since this is for IPv4, it's 32 bits, so set the sign value of
            // the int to "negative"...
            int shiftby = (1 << 31);
            // For the number of bits of the prefix -1 (we already set the sign bit)
            for (int i = netPrefix - 1; i > 0; i--) {
                // Shift the sign right... Java makes the sign bit sticky on a shift...
                // So no need to "set it back up"...
                shiftby = (shiftby >> 1);
            }
            // Transform the resulting value in xxx.xxx.xxx.xxx format, like if
            /// it was a standard address...
            String maskString = Integer.toString((shiftby >> 24) & 255) + "." + Integer.toString((shiftby >> 16) & 255) + "." + Integer.toString((shiftby >> 8) & 255) + "." + Integer.toString(shiftby & 255);
            // Return the address thus created...
            return InetAddress.getByName(maskString);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        // Something went wrong here...
        return null;
    }

    /**
     * Helper to set string to inetaddress.
     * @param address
     * @return
     * @throws UnknownHostException 
     */
    public static InetAddress toIp(String address) throws UnknownHostException{
        return InetAddress.getByName(address);
    }
    
    /**
     * Sets the subnet.
     * @param subnet 
     */
    final void setSubnetAddress(InetAddress subnet) {
        ipSubnet = subnet;
    }
    
    /**
     * Adds a network event listener.
     * @param l 
     */
    public static synchronized void addEventListener( NetworkEventListener l ) {
        _listeners.add( l );
    }
    
    /**
     * Removes a network event listener.
     * @param l 
     */
    public static synchronized void removeEventListener( NetworkEventListener l ) {
        _listeners.remove( l );
    }
    
    /**
     * Fires a network event.
     * Up/Down/Unavailable.
     * @param EVENTTYPE 
     */
    final synchronized void _fireNetworkEvent(String EVENTTYPE) {
        LOG.debug("Event: {}", EVENTTYPE);
        //// Befor any network services are started create a new certificate.
        switch(EVENTTYPE){
            case NetworkEvent.AVAILABLE:
            try {
                //// Generate new certificate.
                CertGen certGen = new CertGen();
            } catch (CertGenException ex) {
                LOG.error("Can not use SSL: " + ex.getMessage());
            }
            break;
        }
        NetworkEvent networkEvent = new NetworkEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (NetworkEventListener) listeners.next() ).handleNetworkEvent( networkEvent );
        }
    }
    
}
