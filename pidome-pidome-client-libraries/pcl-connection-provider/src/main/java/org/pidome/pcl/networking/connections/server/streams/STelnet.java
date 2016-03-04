/*
 * Copyright 2014 John.
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

package org.pidome.pcl.networking.connections.server.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.pidome.pcl.networking.CertHandler;
import org.pidome.pcl.networking.CertHandlerException;

/**
 * Secure raw socket connection.
 * @author John
 */
public class STelnet implements TelnetConnectionInterface {

    static {
        Logger.getLogger(STelnet.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * SSL socket.
     */
    private SSLSocket socket;
    
    /**
     * Socket reader.
     */
    private BufferedReader reader;
    /**
     * Socket writer.
     */
    private PrintWriter writer;
    
    /**
     * Data received from socket.
     */
    private String receivedData = "";
    
    /**
     * Socket listeners.
     */
    private static List _listeners = new ArrayList();
    
    /**
     * Remote host.
     */
    private String ip;
    
    /**
     * Remote port.
     */
    private int port;
    
    /**
     * Constructor.
     * @param ip host String.
     * @param port port to connect to.
     * @throws java.net.UnknownHostException When the host can not be found.
     */
    public STelnet(String ip, int port) throws IOException{
        this.ip = ip;
        this.port = port;
        try {
            SSLSocketFactory sslsocketfactory = CertHandler.getContext().getSocketFactory();
            socket = (SSLSocket) sslsocketfactory.createSocket();
            socket.setReuseAddress(true);
        } catch (CertHandlerException ex) {
            throw new IOException("Could not instantiate SSL connection: " + ex.getMessage());
        }
    }
    
    /**
     * Connects.
     * @throws IOException When the connection failed.
     */
    @Override
    public void connect() throws IOException {
        socket.connect(new InetSocketAddress(ip, port), 3000);
        socket.startHandshake();
        ///SSLSession session = socket.getSession();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        _fireServiceEvent(TelnetEvent.EventType.CONNECTIONAVAILABLE);
    }
    
    /**
     * Stream reader.
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
     * Sends data over the socket.
     * @param data The data to be send.
     * @throws IOException When the socket is unavailable.
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
     * Closing the connection.
     */
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
     * Contains the last known data received.
     * @return The data received.
     */
    @Override
    public final String getData(){
        return receivedData;
    }
    
    /**
     * Adds a listener.
     * @param l Listener for socket data.
     */
    @Override
    public synchronized void addEventListener(TelnetEventListener l) {
        if(!_listeners.contains(l)) _listeners.add(l);
    }

    /**
     * Removes a listener.
     * @param l Listener for socket data.
     */
    @Override
    public synchronized void removeEventListener(TelnetEventListener l) {
        if(_listeners.contains(l)) _listeners.remove(l);
    }

    /**
     * Fired when data is received.
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