/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.server;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionDataInterface;


/**
 * low level class for connection data.
 * @author John
 */
public class ServerConnectionData implements ServerConnectionDataInterface {
    
    static {
        Logger.getLogger(ServerConnectionData.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Remote host.
     */
    private String host;
    
    /**
     * Socket port.
     */
    private int rawPort;
    
    /**
     * http port.
     */
    private int httpPort;

    private boolean isSSL = false;
    
    /**
     * Connection data constructor.
     */
    public ServerConnectionData(){}
    
    
    /**
     * Returns if socket is ssl
     * @return boolean true if is ssl, else false.
     */
    @Override
    public final boolean isSSL(){
        return this.isSSL;
    }
    
    /**
     * Sets the connections SSL property.
     * @param isSSL 
     */
    public final void setIsSSL(boolean isSSL){
        this.isSSL = isSSL;
    }
    
    /**
     * Returns the known host.
     * @return The server host.
     */
    @Override
    public final String getHost(){
        return this.host;
    }
    
    /**
     * Returns the socket port.
     * @return The used port.
     */
    @Override
    public final int getSocketPort(){
        return this.rawPort;
    }
    
    /**
     * Returns the http port.
     * @return Port used.
     */
    @Override
    public final int getHttpPort(){
        return this.httpPort;
    }
    
    /**
     * Set's the main host.
     * @param host The host to set.
     */
    @Override
    public final void setHost(String host){
        this.host = host;
    }
    
    /**
     * Set's the socket data.
     * @param port The port to set.
     */
    @Override
    public final void setSocketPort(int port){
        this.rawPort = port;
    }
 
    /**
     * Sets the http data.
     * @param port the http port to set.
     */
    @Override
    public final void setHttpPort(int port){
        this.httpPort = port;
    }
    
}