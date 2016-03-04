/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.dashboard;

/**
 *
 * @author John
 */
public class Dashboard {
    
    private int dashId;
    private String name;
    private String construct;
    
    private int clientId;
    private int personId;
    
    private String clientType;
    
    private String clientName;
    private String personName;
    
    protected Dashboard(){}
    
    protected void setId(int id){
        this.dashId = id;
    }
    
    public int getId(){
        return this.dashId;
    }
    
    protected void setName(String name){
        this.name = name;
    }
    
    public final String getName(){
        return this.name;
    }
    
    protected void setConstruct(String construct){
        this.construct = construct;
    }
    
    public final String getConstruct(){
        return construct;
    }
    
    protected void setClientId(int clientId){
        this.clientId = clientId;
    }
    
    public int getClientId(){
        return this.clientId;
    }
    
    protected void setPersonId(int personId){
        this.personId = personId;
    }
    
    public int getPersonId(){
        return this.personId;
    }
    
    protected void setClientType(String clientType){
        this.clientType = clientType;
    }
    
    public final String getClientType(){
        return this.clientType;
    }
    
    protected void setClientName(String clientName){
        this.clientName = clientName;
    }
    
    public final String getClientName(){
        return clientName;
    }
    
    protected void setPersonName(String personName){
        this.personName = personName;
    }
    
    public final String getPersonName(){
        return personName;
    }
    
}
