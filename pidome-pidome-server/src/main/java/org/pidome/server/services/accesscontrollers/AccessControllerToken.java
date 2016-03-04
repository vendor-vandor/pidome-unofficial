/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.accesscontrollers;

import org.pidome.server.connector.drivers.devices.specials.presence.PersonToken;

/**
 *
 * @author John
 */
public class AccessControllerToken implements PersonToken {

    private String personName;
    private final int personId;
    
    private final int tokenId;
    
    private boolean isMasterToken = false;
    
    private int wrappedDeviceId;
    private String wrappedDeviceName;
    
    private TokenType tokenType;
    
    protected AccessControllerToken(int tokenId, int personId, boolean isMaster, String name){
        this.tokenId = tokenId;
        this.personId = personId;
        if(isMaster){
            this.personName = "Master token";
        } else {
            this.personName = name;
        }
    }

    protected final void setDeviceId(int wrappedDeviceId){
        this.wrappedDeviceId = wrappedDeviceId;
    }
    
    public final int getWrappedDeviceId(){
        return this.wrappedDeviceId;
    }
    
    protected final void setWrappedDeviceName(String name){
        this.wrappedDeviceName = name;
    }
    
    public final String getWrappedDeviceName(){
        return this.wrappedDeviceName;
    }
    
    /**
     * Returns the set tokentype.
     * This is always null unless explicitly set. Normally only available when tokenlists are requested.
     * @return 
     */
    public final TokenType getTokenType(){
        return this.tokenType;
    }
    
    /**
     * Sets a token type from TokenType.
     * @param type 
     */
    protected final void setTokenType(TokenType type){
        this.tokenType = type;
    }

    /**
     * Sets a TokenType type from string.
     * @param typeFromSting 
     */
    protected final void setTokenType(String typeFromSting){
        for(TokenType type:TokenType.values()){
            if(type.toString().equals(typeFromSting)){
                this.tokenType = type;
                break;
            }
        }
    }
    
    /**
     * Returns the id of this token.
     * @return 
     */
    public final int getTokenId(){
        return this.tokenId;
    }
    
    /**
     * Returns the name bound to this token.
     * @return 
     */
    @Override
    public String getPersonName() {
        return this.personName;
    }

    /**
     * REturns the id of the person bound to this token.
     * @return 
     */
    @Override
    public int getPersonId() {
        return this.personId;
    }
    
    /**
     * Returns if this is a master token or not.
     */
    public boolean isMasterToken(){
        return isMasterToken;
    }
    
}
