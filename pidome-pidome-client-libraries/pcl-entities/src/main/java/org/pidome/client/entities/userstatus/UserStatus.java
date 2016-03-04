/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.userstatus;

/**
 * A Single macro.
 * @author John
 */
public class UserStatus {
    
    /**
     * The status id.
     */
    private final int statusId;
    
    /**
     * The name of the status.
     */
    private String statusName;
    
    /**
     * The status description.
     */
    private String description;
    
    /**
     * Constructor with status id.
     * @param statusId the id of the status.
     */
    public UserStatus(int statusId){
        this.statusId = statusId;
    }
    
    /**
     * Returns the statusId id.
     * @return the statusId id.
     */
    public final int getUserStatusId(){
        return this.statusId;
    }
    
    /**
     * Set a status name.
     * @param statusName name of the status.
     */
    protected final void setName(String statusName){
        this.statusName = statusName;
    }
    
    /**
     * Returns the name of the status.
     * @return Status name.
     */
    public final String getName(){
        return this.statusName;
    }
    
    /**
     * Set a status description.
     * @param description name of the status.
     */
    protected final void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the description of the status.
     * @return Status description.
     */
    public final String getDescription(){
        return this.description;
    }
    
}
