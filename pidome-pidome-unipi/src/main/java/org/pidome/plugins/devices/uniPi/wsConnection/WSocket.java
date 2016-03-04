/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.devices.uniPi.wsConnection;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author John
 */
public class WSocket extends WebSocketClient implements WSocketInterface {

    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(WSocket.class);
    
    private static final List _listeners = new ArrayList();

    String receivedData = "";
    
    public WSocket(URI serverURI) {
        super(serverURI, new Draft_17(), null, 5000);
    }

    public final boolean isConnected(){
        try {
            return this.getConnection().isOpen();
        } catch (Exception ex){
            LOG.warn("Could not check if websocket port is open or not.");
            return false;
        }
    }
    
    @Override
    public void onOpen(ServerHandshake sh) {
        _fireServiceEvent(WSocketEvent.EventType.CONNECTIONAVAILABLE);
    }

    @Override
    public void onMessage(String string) {
        receivedData = string;
        _fireServiceEvent(WSocketEvent.EventType.DATARECEIVED);
    }

    @Override
    public void onClose(int i, String string, boolean bln) {
        System.out.println(string);
        _fireServiceEvent(WSocketEvent.EventType.CONNECTIONLOST);
    }

    @Override
    public void onError(Exception excptn) {
        /// Need to be defined.
    }
    
    @Override
    public final String getData(){
        return receivedData;
    }

    public static synchronized void addSocketListener(WSocketEventListener l) {
        if(!_listeners.contains(l)) _listeners.add(l);
    }

    public static synchronized void removeSocketListener(WSocketEventListener l) {
        if(_listeners.contains(l)) _listeners.remove(l);
    }
    
    @Override
    public synchronized void addEventListener(WSocketEventListener l) {
        /// Not used.
    }

    @Override
    public synchronized void removeEventListener(WSocketEventListener l) {
        ////not used.
    }

    private synchronized void _fireServiceEvent(WSocketEvent.EventType EVENTTYPE) {
        WSocketEvent wSocketEvent = new WSocketEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((WSocketEventListener) listeners.next()).handleWSocketEvent(wSocketEvent);
        }
    }

    @Override
    public void reader() {
        /// Not used in wsocket.
    }

    @Override
    public void stop() {
        this.close();
    }
    
}