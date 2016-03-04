/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.server.streams;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.pidome.pcl.networking.connections.server.ServerConnection;

/**
 *
 * @author John
 */
public class WsSocket extends WebSocketClient implements TelnetConnectionInterface {

    static {
        Logger.getLogger(WsSocket.class.getName()).setLevel(Level.ALL);
    }
    
    private static final List _listeners = new ArrayList();

    String receivedData = "";
    
    public WsSocket(URI serverURI) {
        super(serverURI, new Draft_17(), null, 5000);
    }

    public final boolean isConnected(){
        try {
            return this.getConnection().isOpen();
        } catch (Exception ex){
            Logger.getLogger(WsSocket.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    @Override
    public void onOpen(ServerHandshake sh) {
        _fireServiceEvent(TelnetEvent.EventType.CONNECTIONAVAILABLE);
    }

    @Override
    public void onMessage(String string) {
        receivedData = string;
        _fireServiceEvent(TelnetEvent.EventType.DATARECEIVED);
    }

    @Override
    public void onClose(int i, String string, boolean bln) {
        System.out.println(string);
        _fireServiceEvent(TelnetEvent.EventType.CONNECTIONLOST);
    }

    @Override
    public void onError(Exception excptn) {
        /// Need to be defined.
    }
    
    @Override
    public final String getData(){
        Logger.getLogger(ServerConnection.class.getName()).log(Level.FINEST, "Having data: " + receivedData);
        return receivedData;
    }

    public static synchronized void addSocketListener(TelnetEventListener l) {
        if(!_listeners.contains(l)) _listeners.add(l);
    }

    public static synchronized void removeSocketListener(TelnetEventListener l) {
        if(_listeners.contains(l)) _listeners.remove(l);
    }
    
    @Override
    public synchronized void addEventListener(TelnetEventListener l) {
        /// Not used.
    }

    @Override
    public synchronized void removeEventListener(TelnetEventListener l) {
        ////not used.
    }

    private synchronized void _fireServiceEvent(TelnetEvent.EventType EVENTTYPE) {
        TelnetEvent telnetEvent = new TelnetEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((TelnetEventListener) listeners.next()).handleTelnetEvent(telnetEvent);
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