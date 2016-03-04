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

package org.pidome.client.system.network.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.network.CertHandler;
import org.pidome.client.system.network.CertHandlerException;

/**
 *
 * @author John
 */
public class STelnet implements StreamConnectionInterface {

    static Logger LOG = LogManager.getLogger(STelnet.class);
    
    SSLSocket socket;
    
    BufferedReader reader;
    PrintWriter writer;
    
    String receivedData = "";
    
    static List _listeners = new ArrayList();
    
    public STelnet(String ip, int port) throws UnknownHostException, IOException{
        try {
            SSLSocketFactory sslsocketfactory = CertHandler.getContext().getSocketFactory();
            socket = (SSLSocket) sslsocketfactory.createSocket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(ip, port), 3000);
            socket.startHandshake();
            ///SSLSession session = socket.getSession();
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8").newEncoder()), true);
        } catch (CertHandlerException ex) {
            throw new IOException("Could not instantiate SSL connection: " + ex.getMessage());
        }
    }
    
    @Override
    public final void reader(){
        try {
            while((receivedData = reader.readLine())!=null){
                LOG.debug("Received: {}", receivedData);
                _fireServiceEvent(TelnetEvent.DATARECEIVED);
            }
        } catch (IOException ex) {
            LOG.error("Reading error: {}", ex.getMessage());
            _fireServiceEvent(TelnetEvent.CONNECTIONLOST);
        }
    }
    
    @Override
    public final void send(String data){
        if(socket!=null){
            writer.print(data + "\n");
            if (writer.checkError()){
                LOG.error("Writing error: Where is the server?");
                _fireServiceEvent(TelnetEvent.CONNECTIONLOST);
            }
        } else {
            LOG.error("There is no connection with the server");
        }
    }
    
    public final void stop(){
        LOG.debug("stopping stream");
        if(socket!=null){
            try {
                socket.close();
                socket=null;
            } catch (IOException ex) {
                LOG.warn("Socket already closed?");
            }
        }
    }
    
    public final String getData(){
        return receivedData;
    }
    
    public static synchronized void addEventListener(TelnetEventListener l) {
        LOG.debug("Added listener: {}", l.getClass().getName());
        if(!_listeners.contains(l)) _listeners.add(l);
    }

    public static synchronized void removeEventListener(TelnetEventListener l) {
        LOG.debug("Removed listener: {}", l.getClass().getName());
        if(_listeners.contains(l)) _listeners.remove(l);
    }

    private synchronized void _fireServiceEvent(String EVENTTYPE) {
        LOG.debug("Event: {}", EVENTTYPE);
        TelnetEvent telnetEvent = new TelnetEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((TelnetEventListener) listeners.next()).handleTelnetEvent(telnetEvent);
        }
    }
    
}