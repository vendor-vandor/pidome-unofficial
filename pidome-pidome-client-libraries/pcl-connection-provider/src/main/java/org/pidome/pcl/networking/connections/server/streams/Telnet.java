/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.pcl.networking.connections.server.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Non secure raw socket connection.
 * @author John Sirach
 */
public final class Telnet implements TelnetConnectionInterface {

    static {
        Logger.getLogger(Telnet.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Socket.
     */
    private Socket socket;
    
    /**
     * Socket reader.
     */
    private BufferedReader reader;
    /**
     * Socket writer.
     */
    private PrintWriter writer;
    
    /**
     * Last known received data.
     */
    private String receivedData = "";
    
    /**
     * Socket listeners
     */
    private static final List _listeners = new ArrayList();
    
    /**
     * Remote host.
     */
    private final String ip;
    
    /**
     * Remote port.
     */
    private final int port;
    
    /**
     * Constructor.
     * @param ip Remote host.
     * @param port Remote port.
     * @throws java.io.IOException When the socket can not be used.
     */
    public Telnet(String ip, int port) throws IOException{
        this.ip = ip;
        this.port = port;
        socket = new Socket();
        socket.setReuseAddress(true);
    }
    
    /**
     * Connects.
     */
    @Override
    public void connect() throws IOException {
        socket.connect(new InetSocketAddress(ip, port), 3000);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        _fireServiceEvent(TelnetEvent.EventType.CONNECTIONAVAILABLE);
    }
    
    /**
     * Socket reader.
     */
    @Override
    public final void reader(){
        try {
            while((receivedData = reader.readLine())!=null){
                _fireServiceEvent(TelnetEvent.EventType.DATARECEIVED);
            }
        } catch (IOException ex) {
            _fireServiceEvent(TelnetEvent.EventType.CONNECTIONLOST);
        }
    }
    
    /**
     * Socket writer.
     * @param data Data to be written.
     * @throws IOException When data can not be send (socket failure).
     */
    @Override
    public final void send(String data) throws IOException {
        if(socket!=null){
            writer.print(data + "\n");
            if (writer.checkError()){
                _fireServiceEvent(TelnetEvent.EventType.CONNECTIONLOST);
            }
        } else {
            throw new IOException("No socket available");
        }
    }
    
    /**
     * Socket closer.
     */
    @Override
    public final void stop(){
        if(socket!=null){
            try {
                socket.close();
                socket=null;
            } catch (IOException ex) {
            }
            _fireServiceEvent(TelnetEvent.EventType.CONNECTIONLOST);
        }
    }
    
    /**
     * Returns last known data.
     * @return The data received.
     */
    @Override
    public final String getData(){
        return receivedData;
    }
    
    /**
     * Adds a socket listener.
     * @param l Socket events listener.
     */
    @Override
    public synchronized void addEventListener(TelnetEventListener l) {
        if(!_listeners.contains(l)) _listeners.add(l);
    }

    /**
     * Removes a socket listener.
     * @param l Socket events listener.
     */
    @Override
    public synchronized void removeEventListener(TelnetEventListener l) {
        if(_listeners.contains(l)) _listeners.remove(l);
    }

    /**
     * Event fired when data is available or on socket error.
     * @param EVENTTYPE 
     */
    private synchronized void _fireServiceEvent(TelnetEvent.EventType EVENTTYPE) {
        TelnetEvent telnetEvent = new TelnetEvent(this, EVENTTYPE);
        Iterator listeners;
        switch(EVENTTYPE){
            case CONNECTIONLOST:
                listeners = new ArrayList<TelnetEventListener>(_listeners).iterator();
            break;
            default:
                listeners = _listeners.iterator();
            break;
        }
        while (listeners.hasNext()) {
            ((TelnetEventListener) listeners.next()).handleTelnetEvent(telnetEvent);
        }
    }
    
}
