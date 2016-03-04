/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.services.server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.network.streams.STelnet;
import org.pidome.client.system.network.streams.StreamConnectionInterface;
import org.pidome.client.system.network.streams.Telnet;
import org.pidome.client.system.network.streams.TelnetEvent;
import org.pidome.client.system.network.streams.TelnetEventListener;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;

/**
 *
 * @author John Sirach
 */
public class ServerStream implements TelnetEventListener {

    static Logger LOG = LogManager.getLogger(ServerStream.class);
    
    StreamConnectionInterface telnet;
    Thread telnetReaderThread;
    
    boolean isSSL;
    
    static List _listeners = new ArrayList();
    
    public ServerStream(String ip, int port, boolean ssl) throws UnknownHostException, IOException{
        LOG.info("Initializing connection to server");
        isSSL = ssl;
        if(isSSL){
            STelnet.addEventListener(this);
            telnet = new STelnet(ip,port);
        } else {
            Telnet.addEventListener(this);
            telnet = new Telnet(ip,port);            
        }
    }

    public final void start(){
        telnetReaderThread = new Thread(){
            @Override
            public void run(){
                Thread.currentThread().setName("SERVERSTREAM:READER");
                telnet.reader();
            }
        };
        telnetReaderThread.start();
    }
    
    @Override
    public void handleTelnetEvent(final TelnetEvent event) {
        switch(event.getEventType()){
            case TelnetEvent.DATARECEIVED:
                handleReceivedData(((StreamConnectionInterface)event.getSource()).getData());
            break;
            case TelnetEvent.CONNECTIONLOST:
                _fireServiceEvent(ServerStreamEvent.CONNECTIONLOST, null);
            break;
        }
    }
    
    public final void stop(){
        if(telnet!=null){
            LOG.debug("Trying to sign off");
            try {
                telnet.send(PidomeJSONRPC.createExecMethod("ClientService.signOff", "ClientService.signOff"));
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Could not create/send init");
            }
            telnet.stop();
        }
    }
    
    public final void send(String data){
        if(telnet!=null){
            LOG.debug("Send: {}", data);
            telnet.send(data);
        }
    }
    
    void handleReceivedData(final String data){
        LOG.debug("Received: {}", data);
        if(dataType(data.trim()).equals("UNTHREADED")){
            _fireServiceEvent(ServerStreamEvent.DATARECEIVED, data.trim());
        } else {
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    _fireServiceEvent(ServerStreamEvent.DATARECEIVED, data.trim());
                    return null;
                }
            };
            new Thread(task).start();
        }
    }
    
    String dataType(String data){
        if(data.startsWith("AUTH")){
            return "UNTHREADED";
        } else {
            return "THREADED";
        }
    }
    
    public static synchronized void addEventListener(ServerStreamListener l) {
        LOG.debug("Added listener: {}", l.getClass().getName());
        _listeners.add(l);
    }

    public static synchronized void removeEventListener(ServerStreamListener l) {
        LOG.debug("Removed listener: {}", l.getClass().getName());
        _listeners.remove(l);
    }

    private synchronized void _fireServiceEvent(String EVENTTYPE, Object data) {
        LOG.debug("Event: {}", EVENTTYPE);
        ServerStreamEvent serverStreamEvent = new ServerStreamEvent(this, EVENTTYPE);
        serverStreamEvent.setData(data);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((ServerStreamListener) listeners.next()).handleStreamEvent(serverStreamEvent);
        }
    }
    
}
