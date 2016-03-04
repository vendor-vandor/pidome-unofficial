/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.interfaces;

/**
 * Responsible for network interfaces discovery for local machine.
 * @author John
 */
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEvent;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEventListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;

/**
 * Class for searching for net interfaces.
 * @author John Sirach
 */
public class NetInterface implements NetworkAvailabilityProvider {

    /**
     * Listeners.
     */
    private final List<NetworkAvailabilityEventListener> _listeners = new ArrayList();
    
    static {
        Logger.getLogger(NetInterface.class.getName()).setLevel(Level.ALL);
    }

    /**
     * Ip address.
     */
    private static InetAddress ipAddress;
    /**
     * Broadcast address.
     */
    private static InetAddress broadcastAddress;
    /**
     * Subnet address.
     */
    private static InetAddress ipSubnet;
    
    public NetInterface() {
    }

    @Override
    public void discover() throws UnknownHostException {
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
                            setIpAddress(address);
                            InetAddress broadcast = address.getBroadcast();
                            if (broadcast != null) {
                                setBroadcastAddress(broadcast);
                            }
                            InetAddress subnet = getIPv4LocalNetMask(ipAddress, address.getNetworkPrefixLength());
                            if (subnet != null) {
                                setSubnetAddress(subnet);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            fireAvailabilityEvent(NetworkAvailabilityProvider.Status.NETWORKUNAVAILABLE);
        }
        fireAvailabilityEvent(NetworkAvailabilityProvider.Status.NETWORKAVAILABLE);
    }

    @Override
    public InetAddress getIpAddress() throws UnknownHostException {
        if (ipAddress == null) {
            throw new UnknownHostException("No compatible ip address found in autodiscovery.");
        } else {
            return ipAddress;
        }
    }

    @Override
    public InetAddress getSubnetAddress() throws UnknownHostException {
        if (ipSubnet == null) {
            throw new UnknownHostException("No subnet address found in autodiscovery, set static data in system xml config");
        } else {
            return ipSubnet;
        }
    }

    @Override
    public InetAddress getBroadcastAddress() throws UnknownHostException {
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
    private InetAddress getIPv4LocalNetMask(InetAddress ip, int netPrefix) {

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
            
        }
        // Something went wrong here...
        return null;
    }

    private void setIpAddress(InterfaceAddress address) {
        NetInterface.ipAddress = address.getAddress();
    }

    private void setSubnetAddress(InetAddress subnet) {
        NetInterface.ipSubnet = subnet;
    }

    private void setBroadcastAddress(InetAddress broadcast) {
        NetInterface.broadcastAddress = broadcast;
    }

    public final void addEventListener(NetworkAvailabilityEventListener l) {
        _listeners.add(l);
    }

    public final void removeEventListener(NetworkAvailabilityEventListener l) {
        _listeners.remove(l);
    }

    public final synchronized void fireAvailabilityEvent(NetworkAvailabilityProvider.Status eventType) {
        NetworkAvailabilityEvent serviceEvent = new NetworkAvailabilityEvent(this, eventType);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((NetworkAvailabilityEventListener) listeners.next()).handleNetworkAvailabilityEvent(serviceEvent);
        }
    }
    
}