/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Any event regarding the client's login and session.
 * @author John
 */
public final class PCCClientEvent {
    
    private final PCCClientInterface.ClientStatus status;
    private final String message;
    private int errCode = 0;
    
    /**
     * Constructor sets status with message.
     * @param status ClientStatus type.
     * @param errCode The error code (works as supplement on the status).
     * @param message Describing the error code and status.
     */
    public PCCClientEvent(PCCClientInterface.ClientStatus status, int errCode, String message){
        this.status  = status;
        this.message = message;
        this.errCode = errCode;
    }
    
    /**
     * Returns the message.
     * @return Client event's message.
     */
    public final String getMessage(){
        return this.message;
    }
    
    /**
     * Returns the status.
     * @return Returns the client ClientStatus.
     */
    public final PCCClientInterface.ClientStatus getStatus(){
        return this.status;
    }
    
    /**
     * Get the error code.
     * @return The error code of the status.
     */
    public final int getErrorCode(){
        return errCode;
    }
    
}
