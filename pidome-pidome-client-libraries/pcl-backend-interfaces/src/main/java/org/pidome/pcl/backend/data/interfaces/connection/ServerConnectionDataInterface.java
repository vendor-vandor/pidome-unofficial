/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.connection;

/**
 * Interface providing connection data.
 * @author John
 */
public interface ServerConnectionDataInterface {
    /**
     * Returns the known host.
     * @return The hostname.
     */
    public String getHost();
    
    /**
     * Returns the socket port.
     * @return Teh socket port.
     */
    public int getSocketPort();
    
    /**
     * Sets the connection ssl parameter.
     * @param isSSL 
     */
    public void setIsSSL(boolean isSSL);
    
    /**
     * Returns if socket is ssl
     * @return Returns true if there is an SSL socket.
     */
    public boolean isSSL();
    
    /**
     * Returns the http port.
     * @return Returns the http port.
     */
    public int getHttpPort();
    
    /**
     * Set's the main host.
     * @param host The remote host.
     */
    void setHost(String host);
    
    /**
     * Set's the socket data.
     * @param port The remote socket port.
     */
    void setSocketPort(int port);
    
    /**
     * Sets the http data.
     * @param port The remote http port.
     */
    void setHttpPort(int port);
    
}