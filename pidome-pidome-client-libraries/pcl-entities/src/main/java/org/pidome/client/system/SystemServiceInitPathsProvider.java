/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Main provider for system initialization so the system can be informed of data.
 * @author John
 */
public class SystemServiceInitPathsProvider {
    
    /**
     * The http host address.
     */
    String httpHost;
    
    /**
     * The http host port.
     */
    int httpPort;
    
    /**
     * The json rpc location.
     */
    String rpcUrl;
    
    /**
     * Are we secure?
     */
    boolean isSecure = false;
    
    /**
     * Do we got valid data or not.
     */
    boolean setSuccess = false;
    
    /**
     * Sets the http address.
     * @param address the full http address as http://hostnameorip.
     */
    public final void setHttpAddress(String address){
        this.httpHost = address;
    }
    
    /**
     * Sets the http port.
     * @param port remote http port.
     */
    public final void setHttpPort(int port){
        this.httpPort = port;
    }
    
    /**
     * sets the rpc location.
     * @param rpcLoc remote json-rpc path as /jsonrpc.json with full path.
     */
    public final void setRPCLocation(String rpcLoc){
        this.rpcUrl = rpcLoc;
    }
    
    /**
     * Sets if the connection is a secure one or not.
     * @param secure boolean for secure connection or not.
     */
    public final void setIsSecure(boolean secure){
        this.isSecure = secure;
    }
    
    /**
     * Set to true when having valid data;
     * @param ok true when data is successfull.
     */
    public final void setSuccess(boolean ok){
        this.setSuccess = ok;
    }
    
    /**
     * Returns the http address.
     * @return returns the http host address.
     */
    public final String getHttpAddress(){
        return this.httpHost;
    }
    
    /**
     * Returns the http port.
     * @return returns the port used.
     */
    public final int getHttpPort(){
        return this.httpPort;
    }
    
    /**
     * Returns the rpc location.
     * @return returns the full rpc location.
     */
    public final String getRPCUrl(){
        return this.rpcUrl;
    }
    
    /**
     * Returns if the connection is secure.
     * @return returns true if the connection is secure.
     */
    public final boolean getIsSecure(){
        return this.isSecure;
    }
    
    /**
     * Returns true if object contains valid data.
     * @return returns true if all data is succesfull set.
     */
    public final boolean getSuccess(){
        return this.setSuccess;
    }
    
}
