/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.network;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public class NetInterface {

    private static InetAddress ipAddress;
    private static InetAddress broadcastAddress;
    private static InetAddress ipSubnet;
    static Logger LOG = LogManager.getLogger(NetInterface.class);
    final static List _listeners = new ArrayList();

    public NetInterface() {
    }

    private void setNetworkInterfaceParams() throws UnknownHostException {
        //try {
        LOG.debug("Start network connections discovery");
            Enumeration<java.net.NetworkInterface> list;
            try {
                list = java.net.NetworkInterface.getNetworkInterfaces();

                while (list.hasMoreElements()) {
                    java.net.NetworkInterface iface = (java.net.NetworkInterface) list.nextElement();

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
                                NetInterface.setIpAddress(address);
                                LOG.debug("Found ip address" + address);
                                
                                InetAddress broadcast = address.getBroadcast();
                                if (broadcast != null) {
                                    NetInterface.setBroadcastAddress(broadcast);
                                    LOG.debug("Found broadcast address" + broadcast);
                                }
                                InetAddress subnet = getIPv4LocalNetMask(ipAddress, address.getNetworkPrefixLength());
                                if (subnet != null) {
                                    NetInterface.setSubnetAddress(subnet);
                                    LOG.debug("Found subnet " + subnet);
                                }
                                
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                LOG.error("Could not create socket", ex);
                throw new UnknownHostException("Could not create socket.");
            }
            LOG.debug("Network interface loaded");
            _fireServiceEvent(NetInterfaceEvent.NETWORKAVAILABLE);
        //} catch (Exception e) {
        //    LOG.error("Network interface could not be loaded: {}", e.getMessage());
        //    throw new UnknownHostException(e.getMessage());
        //}
    }

    public void createInterfaceParams() throws UnknownHostException {
        setNetworkInterfaceParams();
    }

    public static InetAddress getIpAddress() throws UnknownHostException {
        if (ipAddress == null) {
            throw new UnknownHostException("No ip address found in autodiscovery, set static data in system xml config");
        } else {
            return ipAddress;
        }
    }

    public static InetAddress getSubnetAddress() throws UnknownHostException {
        if (ipSubnet == null) {
            throw new UnknownHostException("No subnet address found in autodiscovery, set static data in system xml config");
        } else {
            return ipSubnet;
        }
    }

    public static InetAddress getBroadcastAddress() throws UnknownHostException {
        if (broadcastAddress == null) {
            throw new UnknownHostException("No broadcast address found in autodiscovery, set static data in system xml config");
        } else {
            return broadcastAddress;
        }
    }

    /*
     * Get network mask for the IP address and network prefix specified...
     * The network mask will be returned has an IP, thus you can
     * print it out with .getHostAddress()...
     */
    private static InetAddress getIPv4LocalNetMask(InetAddress ip, int netPrefix) {

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
        } catch (UnknownHostException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        // Something went wrong here...
        return null;
    }

    private static void setIpAddress(InterfaceAddress address) {
        NetInterface.ipAddress = address.getAddress();
    }

    private static void setSubnetAddress(InetAddress subnet) {
        NetInterface.ipSubnet = subnet;
    }

    private static void setBroadcastAddress(InetAddress broadcast) {
        NetInterface.broadcastAddress = broadcast;
    }

    public static synchronized void addEventListener(NetInterfaceEventListener l) {
        LOG.debug("Added listener: {}", l.getClass().getName());
        _listeners.add(l);
    }

    public static synchronized void removeEventListener(NetInterfaceEventListener l) {
        LOG.debug("Removed listener: {}", l.getClass().getName());
        _listeners.remove(l);
    }

    private synchronized void _fireServiceEvent(String EVENTTYPE) {
        LOG.debug("Event: {}", EVENTTYPE);
        NetInterfaceEvent serviceEvent = new NetInterfaceEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((NetInterfaceEventListener) listeners.next()).handleNetworkInterfaceEvent(serviceEvent);
        }
    }
}
