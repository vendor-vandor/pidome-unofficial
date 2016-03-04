/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionDataInterface;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionInterface;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionListenerException;
import org.pidome.pcl.networking.connections.server.streams.STelnet;
import org.pidome.pcl.networking.connections.server.streams.Telnet;
import org.pidome.pcl.networking.connections.server.streams.TelnetConnectionInterface;
import org.pidome.pcl.networking.connections.server.streams.TelnetEvent;
import org.pidome.pcl.networking.connections.server.streams.TelnetEventListener;
import org.pidome.pcl.networking.connections.server.streams.WsSocket;

/**
 * Main class responsible for the PiDome connection state and initiations.
 * @author John
 */
public final class ServerConnection implements ServerConnectionInterface,TelnetEventListener {

    static {
        Logger.getLogger(ServerConnection.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Server host.
     */
    private String host = "";
    /**
     * Server port.
     */
    private int port    = 0;
    /**
     * If port is ssl
     */
    private boolean SSL = false;
    
    /**
     * The socket used for the connection.
     * Can be either telnet, secure telnet or websocket.
     */
    TelnetConnectionInterface socket;
    
    /**
     * The server connection data.
     */
    ServerConnectionDataInterface connectData;

    /**
     * Possible server connection statuses.
     */
    public enum ServerConnectionStatus {
        /**
         * Connecting to the server.
         */
        CONNECTING,
        /**
         * Connected with the server.
         */
        CONNECTED,
        /**
         * Disconnected from the server.
         */
        DISCONNECTED,
        /**
         * Fail to connect.
         */
        CONNECT_FAILED;
    }
    
    /**
     * Connection profile used.
     */
    public enum Profile {
        /**
         * For clients at fixed locations.
         */
        FIXED,
        /**
         * For clients moving around and swapping locations.
         */
        MOBILE
    }
    
    /**
     * Current connection status.
     */
    private ServerConnectionStatus status = ServerConnectionStatus.DISCONNECTED;
    
    /**
     * List of connection status listeners.
     */
    private final HashSet<ServerConnectionListener> connectionStateListeners = new HashSet<>();
    
    /**
     * List of connection data listeners.
     */
    private final HashSet<ServerConnectionDataListener> connectionDataListeners = new HashSet<>();
    
    /**
     * Main reader thread.
     */
    Thread telnetReaderThread;
    
    /**
     * The connection profile used.
     * Fixed uses raw tcp connections where mobile uses websockets.
     */
    private Profile connectionProfile = Profile.FIXED;
    
    /**
     * Constructor.
     * @param connectionProfile The connection profile used.
     */
    public ServerConnection(Profile connectionProfile){
        this.connectionProfile = connectionProfile;
    }

    /**
     * Returns the used connection profile.
     * @return The connection profile used.
     */
    public final Profile getConnectionProfile(){
        return this.connectionProfile;
    }
    
    /**
     * Adds a connection listener.
     * @param listener Listener for server connection.
     * @throws ServerConnectionListenerException When there already is a listener registered.
     */
    public void addConnectionListener(ServerConnectionListener listener) throws ServerConnectionListenerException {
        if(!connectionStateListeners.contains(listener)){
            connectionStateListeners.add(listener);
        } else {
            throw new ServerConnectionListenerException("Listener already present");
        }
    }

    /**
     * Adds a data connection listener.
     * @param listener Listener for server connection data.
     */
    public void addDataConnectionListener(ServerConnectionDataListener listener) {
        if(!connectionDataListeners.contains(listener)){
            connectionDataListeners.add(listener);
        }
    }
    
    /**
     * Adds a data connection listener.
     * @param listener Listener for server connection data.
     */
    public void removeDataConnectionListener(ServerConnectionDataListener listener) {
        connectionDataListeners.remove(listener);
    }
    
    /**
     * Broadcasts current connection status.
     * @param status 
     */
    private void broadcastConnectionStatus(ServerConnectionStatus status){
        Iterator<ServerConnectionListener> listeners = connectionStateListeners.iterator();
        while (listeners.hasNext()){
            listeners.next().handleConnectionStatus(status);
        }
    }
    
    /**
     * Broadcasts current connection status.
     * @param status 
     */
    private void broadcastConnectionData(String data){
        Iterator<ServerConnectionDataListener> listeners = connectionDataListeners.iterator();
        while (listeners.hasNext()){
            listeners.next().handleConnectionData(data);
        }
    }
    
    /**
     * Returns the current connection status.
     * @return Returns the connection status.
     */
    public ServerConnectionStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the connection data used.
     * @param connectData The connection data.
     */
    @Override
    public void setConnectionData(ServerConnectionDataInterface connectData) {
        this.connectData = connectData;
    }

    /**
     * Returns the connection data.
     * @return Returns the connection data.
     */
    @Override
    public final ServerConnectionDataInterface getConnectionData(){
        return this.connectData;
    }
    
    /**
     * Connects to the server.
     * @throws IOException When the remote host is unreachable for whatever reason.
     */
    @Override
    public void connect() throws IOException {
        Runnable run = () -> {
            try {
                disconnect();
                broadcastConnectionStatus(ServerConnectionStatus.CONNECTING);
                Logger.getLogger(ServerConnection.class.getName()).log(Level.FINE, "Using connection method: {0}", this.connectionProfile);
                switch(connectionProfile){
                    case FIXED:
                        Logger.getLogger(ServerConnection.class.getName()).log(Level.FINE, "Connecting using SSL: {0}", this.connectData.isSSL());
                        if(this.connectData.isSSL()){
                            try {
                                socket = new STelnet(this.connectData.getHost(), this.connectData.getSocketPort());
                            } catch (Exception ex){
                                Logger.getLogger(ServerConnection.class.getName()).log(Level.WARNING, "No SSL connection available", ex);
                                socket = new Telnet(this.connectData.getHost(), this.connectData.getSocketPort());
                            }
                        } else {
                            Logger.getLogger(ServerConnection.class.getName()).log(Level.WARNING, "No SSL connection available");
                            socket = new Telnet(this.connectData.getHost(), this.connectData.getSocketPort());
                        }
                        try {
                            socket.addEventListener(this);
                            socket.connect();
                            telnetReaderThread = new Thread(
                                () -> {
                                    Thread.currentThread().setName("SERVERSTREAM:READER");
                                    socket.reader();
                                }
                            );
                            telnetReaderThread.start();
                        } catch (Exception ex){
                            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
                            broadcastConnectionStatus(ServerConnectionStatus.CONNECT_FAILED);
                            socket.stop();
                        }
                    break;
                    case MOBILE:
                        try {
                            WsSocket.addSocketListener(this);
                            String url = new StringBuilder("ws://").append(this.connectData.getHost()).append(":").append(this.connectData.getSocketPort()).toString();
                            System.out.println(url);
                            socket = new WsSocket(new URI(url));
                            socket.connect();
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
                            throw new IOException(ex.getMessage());
                        }
                    break;
                }
            } catch(IOException ex){
                Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        run.run();
    }
    
    /**
     * Sends data over the socket.
     * @param sendData Data to be send to the server.
     * @throws java.io.IOException When the socket is unavailable.
     */
    public final void sendData(String sendData) throws IOException {
        if(this.socket!=null){
            try {
                this.socket.send(sendData);
            } catch (Exception ex){
                throw new IOException(ex);
            }
        } else {
            throw new IOException("No socket available");
        }
    }
    
    /**
     * Disconnects from the server.
     */
    @Override
    public void disconnect(){
        if(socket!=null){
            switch(connectionProfile){
                case FIXED:
                    socket.removeEventListener(this);
                break;
                case MOBILE:
                    WsSocket.removeSocketListener(this);
                break;
            }
            socket.stop();
            broadcastConnectionStatus(ServerConnectionStatus.DISCONNECTED);
            socket = null;
        }
        if(telnetReaderThread!=null){
            if(telnetReaderThread.isAlive()){
                telnetReaderThread.interrupt();
            }
            telnetReaderThread = null;
        }
    }
    
    /**
     * Handles a socket stream event.
     * @param event Socket (Telnet) event.
     */
    @Override
    public void handleTelnetEvent(TelnetEvent event) {
        switch(event.getEventType()){
            case CONNECTIONAVAILABLE:
                broadcastConnectionStatus(ServerConnectionStatus.CONNECTED);
            break;
            case CONNECTIONLOST:
                disconnect();
            break;
            case DATARECEIVED:
                broadcastConnectionData(event.getSource().getData());
            break;
        }
    }
    
}