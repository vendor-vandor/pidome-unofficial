/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public class Networking implements NetInterfaceEventListener, BroadcastRecieverEventListener {

    NetInterface    iface = new NetInterface();
    BroadcastReciever brd = new BroadcastReciever();

    static Logger LOG = LogManager.getLogger(Networking.class);
    
    static List _listeners = new ArrayList();
    
    Thread brdListener;
    
    public Networking(){
        NetInterface.addEventListener(this);
        BroadcastReciever.addEventListener(this);
    }
    
    public void start(){
        Thread thrd = new Thread(){
            @Override
            public void run(){
                Thread.currentThread().setName("NETWORK");
                try {
                    LOG.debug("Starting network interfaces search");
                    iface.createInterfaceParams();
                } catch (UnknownHostException ex) {
                    LOG.error("No usable interface found: {}", ex.getMessage());
                }
            }
        };
        thrd.start();
        try {
            thrd.join();
        } catch (InterruptedException ex) {
            LOG.error("Could not join network thread");
        }
    }

    @Override
    public void handleNetworkInterfaceEvent(NetInterfaceEvent event) {
        switch(event.getEventType()){
            case NetInterfaceEvent.NETWORKAVAILABLE:
                startBroadcastListener();
            break;
        }
    }

    public final void startBroadcastListener(){
        brdListener = new Thread(){
            @Override
            public final void run(){
                Thread.currentThread().setName("BRDCST:RECEIVER");
                LOG.debug("Started broadcast receive thread");
                brd.start();
            }
        };
        brdListener.start();
    }
    
    public final void stopBroadcastListener(){
        if(brdListener!=null){
            brdListener.interrupt();
        }
        brd.stop();
    }
    
    public final String getBroadcastMessage(){
        return brd.getBroadcastMessage();
    }
    
    @Override
    public void handleBroadcastRecieverEvent(BroadcastReceiverEvent event) {
        switch(event.getEventType()){
            case BroadcastReceiverEvent.BROADCASTRECEIVED:
                _fireServiceEvent(NetworkingEvent.BROADCASTRECEIVED);
            break;
            case BroadcastReceiverEvent.BROADCASTDISABLED:
                LOG.info("Broadcast has been unsuccesful");
                _fireServiceEvent(NetworkingEvent.BROADCASTDISABLED);
            break;
        }
    }
    
    public static synchronized void addEventListener(NetworkingEventListener l) {
        LOG.debug("Added listener: {}", l.getClass().getName());
        if(!_listeners.contains(l)) _listeners.add(l);
    }

    public static synchronized void removeEventListener(NetworkingEventListener l) {
        LOG.debug("Removed listener: {}", l.getClass().getName());
        if(_listeners.contains(l)) _listeners.remove(l);
    }

    private synchronized void _fireServiceEvent(String EVENTTYPE) {
        LOG.debug("New event: {}", EVENTTYPE);
        NetworkingEvent serviceEvent = new NetworkingEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((NetworkingEventListener) listeners.next()).handleNetworkingEvent(serviceEvent);
        }
    }
    
}
