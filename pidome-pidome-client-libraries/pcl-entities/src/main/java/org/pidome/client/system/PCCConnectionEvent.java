/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Event containing connection information.
 * @author John
 */
public final class PCCConnectionEvent {
    
    /**
     * Containing the remote host if available.
     * Defaults to empty String.
     */
    String remoteSocketHost = "";
    /**
     * Containing the remote port if available.
     * Defaults to 0.
     */
    int remoteSocketPort    = 0;
    
    /**
     * True if the remote host socket is an ssl socket.
     */
    private boolean isSocketSSL = false;
    
    /**
     * Sets the secure parameter for the remote socket
     * @param isSecure true to set secure to true.
     */
    public final void isSocketSSLConnection(boolean isSecure){
        isSocketSSL = isSecure;
    }
    
    /**
     * Sets the remote host.
     * @param host The remote host.
     */
    public final void setRemoteSocketAddress(String host){
        remoteSocketHost = host;
    }
    
    /**
     * Sets the remote port.
     * @param port The remote port.
     */
    public final void setRemoteSocketPort(int port){
        remoteSocketPort = port;
    }
    
    /**
     * Returns if the remote socket port is an SSL port.
     * @return Return if the socket connection is secure.
     */
    public final boolean getIsSocketSSLConnection(){
        return isSocketSSL;
    }
    
    /**
     * Returns the remote host.
     * @return String containing the remote host. Defaults to empty string if unavailable.
     */
    public final String getRemoteSocketAddress(){
        return remoteSocketHost;
    }
    
    /**
     * Returns the remote port.
     * Returns the remote port which defaults to 0 if unavailable.
     * @return The remote host socket port.
     */
    public final int getRemoteSocketPort(){
        return this.remoteSocketPort;
    }
    
    
}