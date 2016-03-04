/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;

/**
 *
 * @author John Sirach
 */
public final class BroadcastReciever {

    static Logger LOG = LogManager.getLogger(BroadcastReciever.class);
    static List _listeners = new ArrayList();

    boolean broadcastReceived = false;
    
    String broadcastMessage = "";
    
    DatagramSocket socket;
    
    Timer timer;
    
    public BroadcastReciever() {
    }

    public final void start() {
        boolean doBroadcast = true;
        try {
            if(!AppProperties.getProperty("system", "TELNETADDRESS").equals("") &&
                    !AppProperties.getProperty("system", "TELNETPORT").equals("") &&
                    !AppProperties.getProperty("system", "STREAMSSL").equals("")){
                doBroadcast = false;
            }
        } catch (AppPropertiesException ex) {
            LOG.info("Having predefined variablesn, no broadcast wait needed");
        }
        if(doBroadcast){
            try {
                if(System.getProperty("os.name").startsWith("Windows")){
                    socket = new DatagramSocket(10000, NetInterface.getIpAddress());
                    LOG.debug("Started listening for broadcast on {} port {}", NetInterface.getIpAddress(), 10000);                
                } else {
                    socket = new DatagramSocket(10000, NetInterface.getBroadcastAddress());
                    LOG.debug("Started listening for broadcast on {} port {}", NetInterface.getBroadcastAddress(), 10000);
                }
                socket.setBroadcast(true);
                byte[] buf = new byte[512];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                timer = new Timer();
                timer.schedule(new ShowStopper(), 10000);
                while (true) {
                    try {
                        socket.receive(packet);
                        broadcastReceived = true;
                        broadcastMessage = new String(packet.getData(), 0, packet.getLength());
                        socket.close();
                        socket = null;
                        LOG.debug("Received: {}", broadcastMessage);
                        timer.cancel();
                        timer.purge();
                        timer = null;
                        _fireServiceEvent(BroadcastReceiverEvent.BROADCASTRECEIVED);
                        break;
                    } catch (Exception ex) {
                        LOG.error("Problem receiving broadcast data: {}", ex.getMessage());
                        break;
                    }
                }
            } catch ( UnknownHostException ex) {
                LOG.error("Could not listen for broadcast from server: {}", ex.getMessage());
            } catch (SocketException  ex) {
                _fireServiceEvent(BroadcastReceiverEvent.BROADCASTDISABLED);
            }
        } else {
            _fireServiceEvent(BroadcastReceiverEvent.BROADCASTDISABLED);
        }
    }
    
    class ShowStopper extends TimerTask {
        @Override
        public void run() {
            LOG.error("Timeout in receiving broadcast.");
            stop();
        }
    }
    
    
    public final void stop(){
        if(socket!=null){
            socket.close();
        }
        socket = null;
        _fireServiceEvent(BroadcastReceiverEvent.BROADCASTDISABLED);
    }
    
    public final String getBroadcastMessage(){
        return broadcastMessage;
    }
    
    public static synchronized void addEventListener(BroadcastRecieverEventListener l) {
        LOG.debug("Added listener: {}", l.getClass().getName());
        _listeners.add(l);
    }

    public static synchronized void removeEventListener(BroadcastRecieverEventListener l) {
        LOG.debug("Removed listener: {}", l.getClass().getName());
        _listeners.remove(l);
    }

    private synchronized void _fireServiceEvent(String EVENTTYPE) {
        LOG.debug("Event: {}", EVENTTYPE);
        BroadcastReceiverEvent serviceEvent = new BroadcastReceiverEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((BroadcastRecieverEventListener) listeners.next()).handleBroadcastRecieverEvent(serviceEvent);
        }
    }
    
}
