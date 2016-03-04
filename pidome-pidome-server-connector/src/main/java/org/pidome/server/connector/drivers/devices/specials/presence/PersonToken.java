/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.specials.presence;

/**
 *
 * @author John
 */
public interface PersonToken {
 
    /**
     * Returns the persons name.
     * @return 
     */
    public String getPersonName();
    
    /**
     * The person's id.
     * @return 
     */
    public int getPersonId();
    
    /**
     * The token type used.
     * This makes it possible to have different token types identifying the same person.
     */
    public enum TokenType {
        PIN,NFC,FINGER,FACE,CUSTOM;
    }
    
}