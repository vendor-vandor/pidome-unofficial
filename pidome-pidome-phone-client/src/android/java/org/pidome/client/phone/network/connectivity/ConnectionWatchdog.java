/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.network.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.phone.services.SystemService;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEvent;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityEventListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;

/**
 * Class used for network interface connectivity.
 * @author John
 */
public final class ConnectionWatchdog extends BroadcastReceiver implements NetworkAvailabilityProvider {

    private ConnectivityManager connManager;
    private NetworkInfo netInfo;
    
    private Context context;
    
    private boolean isPrivateConnection = true;
    
    private InetAddress ipAddress;
    private InetAddress subnetAddress;
    private InetAddress broadcastAddress;
    
    private static boolean lastConnectStat = false;
    private static boolean isConnected     = false;
    
    private static int lastNetType    = -1;
    private static int currentNetType = -1;
    
    /**
     * Listeners.
     */
    private static final List<NetworkAvailabilityEventListener> _listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Sets the initial context.
     * This must be called before the watchdog is added to the PCCSystem.
     * @param context 
     */
    public final void setInitialContext(final Context context){
        this.context = context;
        setNetInfo(context);
    }
    
    /**
     * Receives system network changes event.
     * @param context
     * @param intent 
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("ConnectionWatchdog","Received network event");
        setNetInfo(context);
        if(!SystemService.isCalled()){
            Intent startServiceIntent = new Intent(context, SystemService.class);
            context.startService(startServiceIntent);
        }
    }

    /**
     * Set network information.
     * @param context 
     */
    private void setNetInfo(final Context context){
        this.context = context;
        try {
            connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Log.i("ConnectionWatchdog", "Having con manager: " + (connManager!=null));
            if(connManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI){
                netInfo        = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            } else if (connManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE){
                netInfo     = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
            Log.i("ConnectionWatchdog", "Having network info: " + (netInfo!=null));
            try {
                discover();
            } catch (UnknownHostException ex) {
                Logger.getLogger(ConnectionWatchdog.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                fireAvailabilityEvent(NetworkAvailabilityProvider.Status.NETWORKUNAVAILABLE, -1);
            }
        } catch (NullPointerException ex){
            Logger.getLogger(ConnectionWatchdog.class.getName()).log(Level.INFO, "No network available: " + ex.getMessage(), ex);
            fireAvailabilityEvent(NetworkAvailabilityProvider.Status.NETWORKUNAVAILABLE, -1);
        }
    }
    
    /**
     * Discover network information.
     * With wifi it is easy to get hold of address data. With 3/4G we need to 
     * iterate rmnet, this is not done yet, but availability is set. The information
     * is only needed to be able to do auto discovery which is only available for
     * wifi anyway.
     * @throws UnknownHostException 
     */
    @Override
    public void discover() throws UnknownHostException {
        if(netInfo!= null && ((netInfo.getType() == ConnectivityManager.TYPE_WIFI || netInfo.getType() == ConnectivityManager.TYPE_MOBILE) && netInfo.isConnected())){
            Log.i("Discover info", "type: " + netInfo.getType() + ", connected: " + netInfo.isConnected());
            switch(netInfo.getType()){
                case ConnectivityManager.TYPE_WIFI:
                    WifiManager wifi = (WifiManager)this.context.getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo dhcp = wifi.getDhcpInfo();
                    ipAddress        = intToInetAddress(dhcp.ipAddress);
                    subnetAddress    = intToInetAddress(dhcp.netmask);
                    broadcastAddress = createBroadcastAddress(dhcp.ipAddress, dhcp.netmask);
                break;
            }
            fireAvailabilityEvent(NetworkAvailabilityProvider.Status.NETWORKAVAILABLE, netInfo.getType());
        } else {
            ipAddress        = null;
            subnetAddress    = null;
            broadcastAddress = null;
            fireAvailabilityEvent(NetworkAvailabilityProvider.Status.NETWORKUNAVAILABLE, -1);
        }
    }

    /**
     * Get the current ip address.
     * @return the current ip address
     * @throws UnknownHostException when no ip address is known.
     */
    @Override
    public InetAddress getIpAddress() throws UnknownHostException {
        if(ipAddress==null){
            throw new UnknownHostException("No ip address known");
        } else {
            return ipAddress;
        }
    }

    /**
     * Get the current subnet address.
     * @return the current subnet address
     * @throws UnknownHostException when no subnet address is known.
     */
    @Override
    public InetAddress getSubnetAddress() throws UnknownHostException {
        if(subnetAddress==null){
            throw new UnknownHostException("No subnet address known");
        } else {
            return subnetAddress;
        }
    }

    /**
     * Get the current broadcast address.
     * @return the current broadcast address
     * @throws UnknownHostException when no broadcast address is known.
     */
    @Override
    public InetAddress getBroadcastAddress() throws UnknownHostException {
        if(broadcastAddress==null){
            throw new UnknownHostException("No broadcast address known");
        } else {
            return broadcastAddress;
        }
    }

    /**
     * Creates the broadcast address.
     * @param ipAddress current ip address.
     * @param ipNetMask current netmask.
     * @return the broadcast address from ip and netmask.
     * @throws UnknownHostException when no address can be formed.
     */
    private InetAddress createBroadcastAddress(int ipAddress, int ipNetMask) throws UnknownHostException {
        int broadcast = (ipAddress & ipNetMask) | ~ipNetMask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }
    
    /**
     * Forms an InetAddress from an int
     * @param i the ip as int.
     * @return the InetAddress form of the ip.
     * @throws UnknownHostException when no ip address can be formed.
     */
    private InetAddress intToInetAddress(int i) throws UnknownHostException {
        return InetAddress.getByName((i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF));
    }
    
    /**
     * Adds a status listener.
     * @param nl status listener.
     */
    @Override
    public final void addEventListener(NetworkAvailabilityEventListener nl) {
        if(_listeners.contains(nl)) {
            _listeners.remove(nl);
        }
        _listeners.add(nl);
    }

    /**
     * Removes a status listener.
     * @param nl status listener.
     */
    @Override
    public final void removeEventListener(NetworkAvailabilityEventListener nl) {
        _listeners.remove(nl);
    }
    
    /**
     * Fires a status event.
     * This does a couple of checks:
     * 1. If the previous connection status has been different,
     * 2. If the previous connection type has been different (android sometimes switches network type in the broadcast very fast without us knowing type has been UNAVAILABLE
     * 3. If there are any listeners, if none, reset 1 and 2 and try to broadcast the next event.
     * @param eventType availability or non availability event.
     */
    private synchronized void fireAvailabilityEvent(NetworkAvailabilityProvider.Status eventType, int netType) {
        if(_listeners.size()>0){
            switch(eventType){
                case NETWORKUNAVAILABLE:
                    broadcastUnavailable();
                    isConnected = false;
                break;
                case NETWORKAVAILABLE:
                    if(currentNetType != netType && lastConnectStat==true){
                        Log.i("ConnectionWatchdog","Firing event: NETWORKUNAVAILABLE because of network change");
                        broadcastUnavailable();
                        broadcastAvailable();
                    } else if (isConnected == false){
                        broadcastAvailable();
                    }
                    isConnected = true;
                break;
            }
            lastConnectStat=isConnected;
            currentNetType = netType;
        }
    }
    
    private void broadcastAvailable(){
        NetworkAvailabilityEvent serviceEvent = new NetworkAvailabilityEvent(this, NetworkAvailabilityProvider.Status.NETWORKAVAILABLE);
        Iterator<NetworkAvailabilityEventListener> listeners = _listeners.iterator();
        if(listeners.hasNext()){
            while (listeners.hasNext()) {
                listeners.next().handleNetworkAvailabilityEvent(serviceEvent);
            }
        }
    }
    
    private void broadcastUnavailable(){
        NetworkAvailabilityEvent serviceEvent = new NetworkAvailabilityEvent(this, NetworkAvailabilityProvider.Status.NETWORKUNAVAILABLE);
        Iterator<NetworkAvailabilityEventListener> listeners = _listeners.iterator();
        if(listeners.hasNext()){
            while (listeners.hasNext()) {
                listeners.next().handleNetworkAvailabilityEvent(serviceEvent);
            }
        }
    }
}